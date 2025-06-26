package main.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import main.db.UserDAO;
import main.java.app.LoginManager;
import main.model.User;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;
    @FXML private Button signupButton;

    private LoginManager loginManager;
    private UserDAO userDAO;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        userDAO = new UserDAO();

        // Clear any previous messages
        messageLabel.setText("");

        // Add Enter key support for login
        usernameField.setOnKeyPressed(this::handleKeyPress);
        passwordField.setOnKeyPressed(this::handleKeyPress);

        // Focus on username field when screen loads
        usernameField.requestFocus();

        System.out.println("LoginController initialized");
    }

    /**
     * Set the login manager
     * @param loginManager LoginManager instance
     */
    public void setLoginManager(LoginManager loginManager) {
        this.loginManager = loginManager;
    }

    /**
     * Handle login button click
     * @param event ActionEvent
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        performLogin();
    }

    /**
     * Handle signup button click
     * @param event ActionEvent
     */
    @FXML
    private void handleSignup(ActionEvent event) {
        if (loginManager != null) {
            loginManager.showSignupScreen();
        } else {
            showMessage("Error: Login manager not available", "error");
        }
    }

    /**
     * Handle Enter key press for quick login
     * @param event KeyEvent
     */
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            performLogin();
        }
    }

    /**
     * Perform login validation and authentication
     */
    private void performLogin() {
        String email = usernameField.getText().trim();
        String password = passwordField.getText();

        // Clear previous messages
        messageLabel.setText("");

        // Validation
        if (!validateInput(email, password)) {
            return;
        }

        // Disable buttons during login process
        setButtonsDisabled(true);
        showMessage("Logging in...", "info");

        // Perform login in background task to prevent UI freezing
        Task<User> loginTask = new Task<User>() {
            @Override
            protected User call() throws Exception {
                return userDAO.loginUser(email, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            User user = loginTask.getValue();
            setButtonsDisabled(false);

            if (user != null) {
                showMessage("Login successful! Welcome " + user.getFullName(), "success");

                // Clear password field for security
                passwordField.clear();

                // Navigate to dashboard
                if (loginManager != null) {
                    loginManager.authenticated(user);
                } else {
                    showMessage("Error: Cannot proceed to dashboard", "error");
                }
            } else {
                showMessage("Invalid email or password. Please try again.", "error");
                passwordField.clear();
                passwordField.requestFocus();
            }
        });

        loginTask.setOnFailed(e -> {
            setButtonsDisabled(false);
            showMessage("Login failed. Please check your connection.", "error");
            passwordField.clear();
        });

        // Run the task
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();
    }

    /**
     * Validate user input
     * @param email User email
     * @param password User password
     * @return true if valid, false otherwise
     */
    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            showMessage("Please enter your email address.", "error");
            usernameField.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            showMessage("Please enter your password.", "error");
            passwordField.requestFocus();
            return false;
        }

        // Basic email validation
        if (!email.contains("@") || !email.contains(".")) {
            showMessage("Please enter a valid email address.", "error");
            usernameField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Enable/disable buttons during login process
     * @param disabled true to disable, false to enable
     */
    private void setButtonsDisabled(boolean disabled) {
        loginButton.setDisable(disabled);
        signupButton.setDisable(disabled);
        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
    }

    /**
     * Show message to user with appropriate styling
     * @param message Message to display
     * @param type Message type (success, error, info)
     */
    private void showMessage(String message, String type) {
        messageLabel.setText(message);

        switch (type.toLowerCase()) {
            case "error":
                messageLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                break;
            case "success":
                messageLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                break;
            case "info":
                messageLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
                break;
            default:
                messageLabel.setStyle("-fx-text-fill: black;");
        }
    }

    /**
     * Clear form fields
     */
    public void clearForm() {
        usernameField.clear();
        passwordField.clear();
        messageLabel.setText("");
        usernameField.requestFocus();
    }
}