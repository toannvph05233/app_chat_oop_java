package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChatServer {
    private static Set<String> onlineUsers = new HashSet<>();
    private static Map<String, PrintWriter> userWriters = new HashMap<>();
    private static Connection connection = ConnectSQL.getConnect();
    public static void main(String[] args) {
        System.out.println("The chat server is running...");
        try (ServerSocket listener = new ServerSocket(12345)) {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Handler extends Thread {
        private String username;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    String command = in.readLine();
                    if (command.equals("login")) {
                        String username = in.readLine();
                        String password = in.readLine();
                        if (authenticate(username, password)) {
                            this.username = username;
                            out.println("success");
                            synchronized (onlineUsers) {
                                onlineUsers.add(username);
                                userWriters.put(username, out);
                                broadcastOnlineUsers();
                            }
                            break;
                        } else {
                            out.println("failure");
                        }
                    } else if (command.equals("register")) {
                        String username = in.readLine();
                        String password = in.readLine();
                        if (register(username, password)) {
                            out.println("success");
                        } else {
                            out.println("failure");
                        }
                    }
                }

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("private ")) {
                        int firstSpace = message.indexOf(' ');
                        int secondSpace = message.indexOf(' ', firstSpace + 1);
                        String targetUser = message.substring(firstSpace + 1, secondSpace);
                        String privateMessage = message.substring(secondSpace + 1);
                        sendPrivateMessage(targetUser, privateMessage);
                    } else {
                        broadcastMessage(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (username != null) {
                    synchronized (onlineUsers) {
                        onlineUsers.remove(username);
                        userWriters.remove(username);
                        broadcastOnlineUsers();
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean authenticate(String username, String password) {
            try {
                PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean register(String username, String password) {
            try {
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }


        private void broadcastMessage(String message) {
            synchronized (onlineUsers) {
                for (PrintWriter writer : userWriters.values()) {
                    writer.println(message);
                }
            }
        }

        private void sendPrivateMessage(String targetUser, String message) {
            synchronized (onlineUsers) {
                PrintWriter targetWriter = userWriters.get(targetUser);
                if (targetWriter != null) {
                    targetWriter.println("PRIVATE " + username + " " + message);
                }
            }
        }

        private void broadcastOnlineUsers() {
            StringBuilder usersList = new StringBuilder();
            for (String user : onlineUsers) {
                usersList.append(user).append(",");
            }
            if (usersList.length() > 0) {
                usersList.setLength(usersList.length() - 1); // remove last comma
            }
            String onlineUsersMessage = "ONLINE_USERS " + usersList.toString();
            for (PrintWriter writer : userWriters.values()) {
                writer.println(onlineUsersMessage);
            }
        }
    }
}
