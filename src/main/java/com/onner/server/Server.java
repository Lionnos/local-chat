package com.onner.server;

import java.net.ServerSocket;

public class Server {
    public static void main(String[] args) throws Exception {

        int PORT = 9003;

        ServerSocket listener = new ServerSocket(PORT);
        System.out.println("The server is running...");
        try {
            while (true) {
                new UserHandler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }
}
