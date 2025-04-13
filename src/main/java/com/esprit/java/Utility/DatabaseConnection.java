package com.esprit.java.Utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    // Database connection parameters
    private static final String URL = "jdbc:mysql://localhost:3306/learniverse";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    // Private constructor to prevent instantiation
    private DatabaseConnection() {
    }
    
    /**
     * Get a connection to the database
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Return a new connection
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found", e);
        }
    }
} 