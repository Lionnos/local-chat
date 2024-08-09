package com.onner.server;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserManager {
    private static Map<String, PrintWriter> userWriters = new HashMap<>();
    private static Map<String, Socket> userSockets = new HashMap<>();

    public static synchronized boolean registerUser(String name, PrintWriter out, Socket socket) {
        if (userWriters.containsKey(name)) {
            return false;
        }
        userWriters.put(name, out);
        userSockets.put(name, socket);
        return true;
    }

    public static synchronized void unregisterUser(String name) {
        if (name != null) {
            userWriters.remove(name);
            userSockets.remove(name);
        }
    }

    public static synchronized PrintWriter getClientWriter(String name) {
        return userWriters.get(name);
    }

    public static synchronized void broadcastUserList() {
        StringBuilder userList = new StringBuilder("USER_LIST");
        Set<String> userNames = userWriters.keySet();
        for (String userName : userNames) {
            userList.append(" ").append(userName);
        }
        for (PrintWriter writer : userWriters.values()) {
            writer.println(userList);
        }
        System.out.println("SERVER: Update: " + userList);
    }
}
