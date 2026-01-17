package com.arnobx86.smarthomeexpensemanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {
    // Connect to MySQL server (no database selected) to create the DB
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "smart_home_expense_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Cicada3301"; 

    public static void initialize() {
        try {
            // Explicitly load the driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
            return;
        }

        try (Connection conn = DriverManager.getConnection(SERVER_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // Create Database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            
            // Switch to the database
            stmt.executeUpdate("USE " + DB_NAME);
            
            // Create Users Table
            String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL" +
                    ") ENGINE=InnoDB";
            stmt.executeUpdate(createUsers);
            
            // Insert Default User (arnob/arnobx86)
            stmt.executeUpdate("INSERT IGNORE INTO users (username, password) VALUES ('arnob', 'arnobx86')");
            
            // Create Categories Table
            String createCategories = "CREATE TABLE IF NOT EXISTS categories (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(50) NOT NULL UNIQUE" +
                    ") ENGINE=InnoDB";
            stmt.executeUpdate(createCategories);
            
            // Insert Default Categories
            String[] categories = {"Food", "Transport", "Utilities", "Entertainment", "Health", "Others"};
            for (String cat : categories) {
                stmt.executeUpdate("INSERT IGNORE INTO categories (name) VALUES ('" + cat + "')");
            }
            
            // Create Expenses Table
            String createExpenses = "CREATE TABLE IF NOT EXISTS expenses (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "category VARCHAR(50), " +
                    "amount DOUBLE NOT NULL, " +
                    "date DATE NOT NULL, " +
                    "description VARCHAR(255), " +
                    "FOREIGN KEY (category) REFERENCES categories(name) ON DELETE SET NULL ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB";
            stmt.executeUpdate(createExpenses);
            
            System.out.println("Database initialized successfully.");
            
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
