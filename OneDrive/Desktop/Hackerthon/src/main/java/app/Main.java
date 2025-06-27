package main.java.app;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import main.db.DatabaseConnection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Test database connection first
            if (!DatabaseConnection.testConnection()) {
                showDatabaseError();
                return;
            }

            // Set up the stage
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            // Create login manager and show login screen
            LoginManager loginManager = new LoginManager(primaryStage);
            loginManager.showLoginScreen();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Application Error", "Failed to start application: " + e.getMessage());
        }
    }

    private void showDatabaseError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Connection Error");
        alert.setHeaderText("Cannot connect to database");
        alert.setContentText("Please make sure:\n" +
                "1. XAMPP/MySQL is running\n" +
                "2. MySQL service is started\n" +
                "3. Database 'collegeapp' exists\n" +
                "4. MySQL connector JAR is in classpath\n" +
                "5. database.properties is configured properly");
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
