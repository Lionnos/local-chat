package com.onner.server;

import java.io.PrintWriter;

public class MessageHandler {

    public static void handleMessage(String sender, String message) {
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String[] userParts = parts[0].split("@");
            if (userParts.length == 2) {
                String receiver = userParts[1].trim();
                PrintWriter writer = UserManager.getClientWriter(receiver);
                if (writer != null) {
                    writer.println(message);
                    //System.out.println("SERVER: Message sent from " + sender + " to " + receiver + ": " + message);
                } else {
                    System.out.println("SERVER: The user " + receiver + " is not logged in.");
                }
            }
        }
    }
}