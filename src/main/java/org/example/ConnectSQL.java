package org.example;

import java.sql.*;


public class ConnectSQL {
    public static Connection getConnect() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/chat_app", "root", "12345678");
        } catch (SQLException e) {
            return null;
        }
    }

}
