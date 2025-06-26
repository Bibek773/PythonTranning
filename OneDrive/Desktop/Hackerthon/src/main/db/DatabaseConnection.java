package main.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Database credentials for XAMPP
    private static final String URL = "jdbc:mysql://localhost:3306/collegeapp";
    private static final String USERNAME = "root";
    private static final String PASSWORD = ""; // Empty for XAMPP default

    // Database driver
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Get database connection
     * @return Connection object or null if failed
     */
    public static Connection getConnection() {
        Connection connection = null;

        try {
            // Load MySQL JDBC Driver
            Class.forName(DRIVER);

            // Create connection
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            System.out.println("Database connected successfully!");

        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            System.err.println("Make sure you have mysql-connector-java in your classpath");
            e.printStackTrace();

        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            System.err.println("Check if XAMPP MySQL is running and database 'collegeapp' exists");
            e.printStackTrace();
        }

        return connection;
    }

    /**
     * Test database connection
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Close database connection safely
     * @param connection Connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}