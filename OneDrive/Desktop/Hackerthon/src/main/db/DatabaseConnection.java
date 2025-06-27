package main.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Executors;

public class DatabaseConnection {

    private static String DB_HOST;
    private static String DB_PORT;
    private static String DB_NAME;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    private static String DB_URL;

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    static {
        loadDatabaseConfig();
    }

    private static void loadDatabaseConfig() {
        Properties props = new Properties();

        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("config/database.properties")) {

            if (input == null) {
                System.err.println("Database config file not found! Using default values.");
                setDefaultConfig();
                return;
            }

            props.load(input);

            DB_HOST = props.getProperty("db.host", "localhost");
            DB_PORT = props.getProperty("db.port", "3306");
            DB_NAME = props.getProperty("db.name", "collegeapp");
            DB_USERNAME = props.getProperty("db.username", "root");
            DB_PASSWORD = props.getProperty("db.password", "");

            DB_URL = String.format(
                    "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    DB_HOST, DB_PORT, DB_NAME);

            System.out.println("Database configuration loaded successfully");
            System.out.println("Connecting to: " + DB_HOST + ":" + DB_PORT + "/" + DB_NAME);

        } catch (IOException e) {
            System.err.println("Error loading database configuration: " + e.getMessage());
            setDefaultConfig();
        }
    }

    private static void setDefaultConfig() {
        DB_HOST = "localhost";
        DB_PORT = "3306";
        DB_NAME = "collegeapp";
        DB_USERNAME = "root";
        DB_PASSWORD = "";
        DB_URL = "jdbc:mysql://localhost:3306/collegeapp?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        System.out.println("Using default local database configuration");
    }

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            // Use a valid Executor to avoid SQLException
            connection.setNetworkTimeout(Executors.newSingleThreadExecutor(), 30000); // 30 sec timeout

            System.out.println("Database connected successfully to: " + DB_HOST);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found! Add it to your project.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            System.err.println("Host: " + DB_HOST + ":" + DB_PORT);
            System.err.println("Database: " + DB_NAME);
            System.err.println("Error: " + e.getMessage());
            if (e.getMessage().contains("Connection refused")) {
                System.err.println("Solution: Check if MySQL server is running on " + DB_HOST);
            } else if (e.getMessage().contains("Unknown database")) {
                System.err.println("Solution: Create database '" + DB_NAME + "' on " + DB_HOST);
            } else if (e.getMessage().contains("Access denied")) {
                System.err.println("Solution: Check username/password for " + DB_USERNAME);
            }
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    public static boolean testConnection() {
        System.out.println("Testing database connection...");
        System.out.println("Host: " + DB_HOST + ":" + DB_PORT);
        System.out.println("Database: " + DB_NAME);
        System.out.println("Username: " + DB_USERNAME);
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Database connection test PASSED");
                return true;
            } else {
                System.err.println("✗ Database connection test FAILED - Connection is null or closed");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("✗ Database connection test FAILED: " + e.getMessage());
            return false;
        }
    }

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

    public static String getConnectionInfo() {
        return String.format("Host: %s:%s, Database: %s, User: %s", DB_HOST, DB_PORT, DB_NAME, DB_USERNAME);
    }

    public static void reloadConfig() {
        loadDatabaseConfig();
    }
}
