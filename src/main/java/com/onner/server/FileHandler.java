package com.onner.server;

import java.io.PrintWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class FileHandler {
    private static Map<String, byte[]> fileBuffer = new HashMap<>();
    private static Map<String, String> fileNames = new HashMap<>();

    public static void handleFileMessage(String sender, String message) {
        String[] parts = message.split(":", 4);
        if (parts.length == 4) {
            String senderReceiver = parts[1];
            String fileName = parts[2];
            byte[] fileContent = Base64.getDecoder().decode(parts[3]);

            if (fileContent == null || fileContent.length == 0) {
                System.out.println("SERVER: The content of the received file is null or empty.");
            } else {
                String key = senderReceiver + ":" + fileName;
                fileBuffer.put(key, fileContent);
                fileNames.put(key, fileName);

                System.out.println("SERVER: File stored on server: " + fileName + ", Key: " + key + ", Size: " + fileContent.length);

                String[] users = senderReceiver.split("@");
                if (users.length == 2) {
                    String receiver = users[1].trim();
                    PrintWriter writer = UserManager.getClientWriter(receiver);
                    if (writer != null) {
                        writer.println("FILE:" + senderReceiver + ":" + fileName + ":" + Base64.getEncoder().encodeToString(fileContent));
                        System.out.println("SERVER: Notification of file sent to " + receiver + ": " + key);
                    }
                }
            }
        }
    }

    public static void handleUpdateFileList(String requester, String message) {
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String[] userParts = parts[1].split("@");
            if (userParts.length == 2) {
                String sender = userParts[1].trim();
                PrintWriter writer = UserManager.getClientWriter(requester);
                if (writer != null) {
                    for (Map.Entry<String, String> entry : fileNames.entrySet()) {
                        String key = entry.getKey();
                        if (key.startsWith(sender + "@")) {
                            writer.println("FILE_AVAILABLE:" + key);
                            System.out.println("SERVER: Updated file list for " + requester + ": " + key);
                        }
                    }
                }
            }
        }
    }
}
