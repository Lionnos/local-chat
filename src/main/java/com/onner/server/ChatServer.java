package com.onner.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<String, ClientHandler> clientHandlers = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private String clientId;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Register the client
                out.println("Enter your ID:");
                clientId = in.readLine();
                synchronized (clientHandlers) {
                    clientHandlers.put(clientId, this);
                }
                System.out.println("Client " + clientId + " connected");
                sendClientList();

                String message;
                while ((message = in.readLine()) != null) {
                    processMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientHandlers) {
                    clientHandlers.remove(clientId);
                }
                System.out.println("Client " + clientId + " disconnected");
                sendClientList();
            }
        }

        private void processMessage(String message) {
            String[] parts = message.split(":", 4);
            String type = parts[0];
            String sender = parts[1];
            String receiver = parts[2];
            String content = parts[3];

            ClientHandler receiverHandler;
            synchronized (clientHandlers) {
                receiverHandler = clientHandlers.get(receiver);
            }

            if (receiverHandler != null) {
                receiverHandler.out.println(type + ":" + sender + ":" + content);
            } else {
                out.println("User " + receiver + " not found");
            }
        }

        private void sendClientList() {
            StringBuilder clientList = new StringBuilder("CLIENT_LIST:");
            synchronized (clientHandlers) {
                for (String id : clientHandlers.keySet()) {
                    clientList.append(id).append(",");
                }
            }
            String clientListMessage = clientList.toString();
            for (ClientHandler handler : clientHandlers.values()) {
                handler.out.println(clientListMessage);
            }
        }
    }
}
