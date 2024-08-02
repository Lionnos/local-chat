package com.onner.client;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ChatClient {
        private static final String SERVER_ADDRESS = "200.200.3.13";
        private static final int SERVER_PORT = 12345;
        private static Set<String> connectedClients = new HashSet<>();

        public static void main(String[] args) {
            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter your ID:");
                String clientId = scanner.nextLine();
                out.println(clientId);

                Thread readThread = new Thread(() -> {
                    try {
                        String response;
                        while ((response = in.readLine()) != null) {
                            processServerMessage(response);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                readThread.start();

                while (true) {
                    System.out.println("Enter receiver's ID and message (format: receiver:message):");
                    String input = scanner.nextLine();
                    String[] parts = input.split(":", 2);
                    String receiver = parts[0];
                    String message = parts[1];
                    out.println("MSG:" + clientId + ":" + receiver + ":" + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static void processServerMessage(String message) {
            if (message.startsWith("CLIENT_LIST:")) {
                updateClientList(message.substring(12));
            } else {
                System.out.println(message);
            }
        }

        private static void updateClientList(String clientList) {
            connectedClients.clear();
            String[] clients = clientList.split(",");
            for (String client : clients) {
                if (!client.isEmpty()) {
                    connectedClients.add(client);
                }
            }
            System.out.println("Connected clients: " + connectedClients);
        }
    }
