package com.onner.server;

import java.net.ServerSocket;

public class Server {
    public static void main(String[] args) throws Exception {

        int ServerPort = 66669;

        ServerSocket listener = new ServerSocket(ServerPort);
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