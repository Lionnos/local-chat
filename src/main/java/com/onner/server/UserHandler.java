package com.onner.server;

import java.io.*;
import java.net.Socket;

public class UserHandler extends Thread {
    private String name;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public UserHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            name = in.readLine();
            if (UserManager.registerUser(name, out, socket)) {
                System.out.println("SERVER: Logged in user: " + name);
                UserManager.broadcastUserList();

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("FILE:")) {
                        System.out.println("SERVER: Archive message received");
                        FileHandler.handleFileMessage(name, message);
                    } else if (message.startsWith("UPDATE_FILE_LIST:")) {
                        FileHandler.handleUpdateFileList(name, message);
                    } else {
                        //System.out.println("SERVER: Message received from " + name + ": " + message);
                        MessageHandler.handleMessage(name, message);
                    }
                }
            } else {
                out.println("ERROR: Username already in use");
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            UserManager.unregisterUser(name);
            System.out.println("SERVER: Disconnected user: " + name);
            UserManager.broadcastUserList();
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }
}
