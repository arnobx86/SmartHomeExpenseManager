package com.arnobx86.smarthomeexpensemanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseCleaner {
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "smart_home_expense_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Cicada3301";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
            return;
        }

        try (Connection conn = DriverManager.getConnection(SERVER_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            System.out.println("Dropping database '" + DB_NAME + "'...");
            stmt.executeUpdate("DROP DATABASE IF EXISTS " + DB_NAME);
            System.out.println("Database deleted successfully.");
            System.out.println("Run the application again to recreate the database with default data.");

        } catch (SQLException e) {
            System.err.println("Error clearing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
