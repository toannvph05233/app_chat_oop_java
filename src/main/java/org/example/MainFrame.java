package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

public class MainFrame extends JFrame {
    private ChatClient chatClient;
    private JTextArea messageArea;
    private JTextField inputField;
    private JButton sendButton;
    private JList<String> onlineUserList;
    private DefaultListModel<String> listModel;
    private JButton chatAllButton;
    private String currentChatUser;

    public MainFrame(ChatClient chatClient) {
        new Thread(new IncomingReader()).start();
        this.chatClient = chatClient;
        setTitle("Chat - " + chatClient.getUsername());
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        inputField = new JTextField();
        sendButton = new JButton("Send");

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(inputField, BorderLayout.CENTER);
        southPanel.add(sendButton, BorderLayout.EAST);

        listModel = new DefaultListModel<>();
        onlineUserList = new JList<>(listModel);
        onlineUserList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        onlineUserList.setLayoutOrientation(JList.VERTICAL);
        onlineUserList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedUser = onlineUserList.getSelectedValue();
                    if (selectedUser != null && !selectedUser.equals(currentChatUser)) {
                        currentChatUser = selectedUser;
                        displayPrivateMessages();
                    }
                }
            }
        });

        chatAllButton = new JButton("Chat All");
        chatAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentChatUser = null;
                displayAllMessages();
            }
        });

        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.add(new JLabel("Online Users"), BorderLayout.NORTH);
        eastPanel.add(new JScrollPane(onlineUserList), BorderLayout.CENTER);
        eastPanel.add(chatAllButton, BorderLayout.SOUTH);

        add(eastPanel, BorderLayout.EAST);
        panel.add(southPanel, BorderLayout.SOUTH);

        add(panel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText();
                if (!message.isEmpty()) {
                    if (currentChatUser == null) {
                        chatClient.sendMessage(message);
                    } else {
                        chatClient.sendPrivateMessage(currentChatUser, message);
                        displayPrivateMessages();
                    }
                    inputField.setText("");
                }
            }
        });

    }

    private class IncomingReader implements Runnable {
        public void run() {
            String message;
            try {
                while ((message = chatClient.receiveMessage()) != null) {
                    if (message.startsWith("ONLINE_USERS ")) {
                        chatClient.setOnlineUsers(message.substring(13));
                        updateOnlineUserList();
                    } else if (message.startsWith("PRIVATE ")) {
                        String[] parts = message.split(" ", 3);
                        String sender = parts[1];
                        String privateMessage = parts[2];
                        chatClient.addPrivateMessage(sender, sender + ": " + privateMessage);
                        if (currentChatUser != null && currentChatUser.equals(sender)) {
                            messageArea.append(sender + ": " + privateMessage + "\n");
                        }
                    } else {
                        chatClient.addAllMessage(message);
                        if (currentChatUser == null) {
                            messageArea.append(message + "\n");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateOnlineUserList() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                listModel.clear();
                List<String> onlineUsers = chatClient.getOnlineUsers();
                for (String user : onlineUsers) {
                    listModel.addElement(user);
                }
            }
        });
    }

    private void displayAllMessages() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                messageArea.setText("");
                List<String> messages = chatClient.getAllMessages();
                for (String msg : messages) {
                    messageArea.append(msg + "\n");
                }
            }
        });
    }

    private void displayPrivateMessages() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                messageArea.setText("");
                List<String> messages = chatClient.getPrivateMessages(currentChatUser);
                for (String msg : messages) {
                    messageArea.append(msg + "\n");
                }
            }
        });
    }
}
