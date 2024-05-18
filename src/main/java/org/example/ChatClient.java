package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private List<String> onlineUsers = new ArrayList<>();
    private List<String> allMessages = new ArrayList<>();
    private Map<String, List<String>> privateMessages = new HashMap<>();

    public boolean connect(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String username, String password) {
        out.println("login");
        out.println(username);
        out.println(password);
        this.username = username;
        try {
            String result = in.readLine();
            return (result.contains("ONLINE_USERS")|| "success".equals(result));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean register(String username, String password) {
        out.println("register");
        out.println(username);
        out.println(password);
        try {
            return "success".equals(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendMessage(String message) {
        out.println(message);
        addAllMessage("You: " + message);
    }

    public void sendPrivateMessage(String targetUser, String message) {
        out.println("private " + targetUser + " " + message);
        addPrivateMessage(targetUser, "You: " + message);
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }

    public void close() throws IOException {
        socket.close();
    }

    public String getUsername() {
        return username;
    }

    public List<String> getOnlineUsers() {
        return onlineUsers;
    }

    public void setOnlineUsers(String usersList) {
        onlineUsers.clear();
        if (!usersList.isEmpty()) {
            String[] users = usersList.split(",");
            for (String user : users) {
                onlineUsers.add(user);
            }
        }
    }

    public void addAllMessage(String message) {
        allMessages.add(message);
    }

    public List<String> getAllMessages() {
        return allMessages;
    }

    public void addPrivateMessage(String user, String message) {
        privateMessages.computeIfAbsent(user, k -> new ArrayList<>()).add(message);
    }

    public List<String> getPrivateMessages(String user) {
        return privateMessages.getOrDefault(user, new ArrayList<>());
    }
}
