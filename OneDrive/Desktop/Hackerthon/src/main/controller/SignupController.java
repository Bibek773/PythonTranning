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
import main.utils.EmailSender;
import main.utils.OTPGenerator;

public class SignupController {
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> userTypeCombo;
    @FXML private TextField rollNoField;
    @FXML private ComboBox<Integer> semesterCombo;
    @FXML private ComboBox<String> facultyCombo;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField otpField;
    @FXML private Label messageLabel;
    @FXML private Button sendOtpButton;
    @FXML private Button verifyOtpButton;
    @FXML private Button signupButton;
    @FXML private Button loginButton;
    @FXML private Label rollNoLabel;
    @FXML private Label semesterLabel;

    private LoginManager loginManager;
    private UserDAO userDAO;
    private EmailSender emailSender;
    private String generatedOtp;
    private boolean otpVerified = false;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        emailSender = new EmailSender();

        setupComboBoxes();
        setupEventHandlers();

        // Initially disable OTP and signup controls
        if (otpField != null) {
            otpField.setDisable(true);
        }
        if (verifyOtpButton != null) {
            verifyOtpButton.setDisable(true);
        }
        if (signupButton != null) {
            signupButton.setDisable(true);
        }

        if (messageLabel != null) {
            messageLabel.setText("");
        }
        System.out.println("SignupController initialized");
    }

    private void setupComboBoxes() {
        // User type combo
        if (userTypeCombo != null) {
            userTypeCombo.getItems().addAll("STUDENT", "TEACHER");
            userTypeCombo.setValue("STUDENT");
            // Handle user type change
            userTypeCombo.setOnAction(e -> handleUserTypeChange());
        }

        // Faculty combo
        if (facultyCombo != null) {
            facultyCombo.getItems().addAll(
                    "Engineering", "Management", "Science", "Arts",
                    "Medicine", "Law", "Education", "Agriculture"
            );
        }

        // Semester combo (1-8)
        if (semesterCombo != null) {
            for (int i = 1; i <= 8; i++) {
                semesterCombo.getItems().add(i);
            }
        }
    }

    private void setupEventHandlers() {
        // Enter key support - only set if fields exist
        if (emailField != null) {
            emailField.setOnKeyPressed(this::handleKeyPress);
            // Real-time email validation
            emailField.textProperty().addListener((obs, oldText, newText) -> {
                if (!newText.trim().isEmpty() && isValidEmail(newText)) {
                    if (sendOtpButton != null) {
                        sendOtpButton.setDisable(false);
                    }
                } else {
                    if (sendOtpButton != null) {
                        sendOtpButton.setDisable(true);
                    }
                    otpVerified = false;
                    resetOtpFields();
                }
            });
        }

        if (usernameField != null) {
            usernameField.setOnKeyPressed(this::handleKeyPress);
            // Real-time username validation
            usernameField.textProperty().addListener((obs, oldText, newText) -> {
                validateUsernameRealTime(newText);
            });
        }

        if (fullNameField != null) {
            fullNameField.setOnKeyPressed(this::handleKeyPress);
        }

        if (phoneField != null) {
            phoneField.setOnKeyPressed(this::handleKeyPress);
        }

        if (passwordField != null) {
            passwordField.setOnKeyPressed(this::handleKeyPress);
        }

        if (confirmPasswordField != null) {
            confirmPasswordField.setOnKeyPressed(this::handleKeyPress);
        }

        if (otpField != null) {
            otpField.setOnKeyPressed(this::handleKeyPress);
        }
    }

    private void validateUsernameRealTime(String username) {
        if (username.trim().isEmpty()) {
            return;
        }

        // Basic username validation
        if (!isValidUsername(username)) {
            showMessage("Username can only contain letters, numbers, and underscores", "error");
        } else if (userDAO.usernameExists(username.trim())) {
            showMessage("Username already taken", "error");
        } else {
            // Clear error message if username is valid and available
            if (messageLabel != null && messageLabel.getText().contains("Username")) {
                messageLabel.setText("");
            }
        }
    }

    public void setLoginManager(LoginManager loginManager) {
        this.loginManager = loginManager;
    }

    @FXML
    private void handleUserTypeChange() {
        if (userTypeCombo == null) return;

        String userType = userTypeCombo.getValue();
        boolean isStudent = "STUDENT".equals(userType);

        if (rollNoField != null) {
            rollNoField.setVisible(isStudent);
            rollNoField.setManaged(isStudent);
        }

        if (rollNoLabel != null) {
            rollNoLabel.setVisible(isStudent);
            rollNoLabel.setManaged(isStudent);
        }

        if (semesterCombo != null) {
            semesterCombo.setVisible(isStudent);
            semesterCombo.setManaged(isStudent);
        }

        if (semesterLabel != null) {
            semesterLabel.setVisible(isStudent);
            semesterLabel.setManaged(isStudent);
        }

        if (!isStudent) {
            if (rollNoField != null) {
                rollNoField.clear();
            }
            if (semesterCombo != null) {
                semesterCombo.setValue(null);
            }
        }
    }

    @FXML
    private void handleSendOtp(ActionEvent event) {
        if (emailField == null) return;

        String email = emailField.getText().trim();

        if (!isValidEmail(email)) {
            showMessage("Please enter a valid email address", "error");
            return;
        }

        // Check if email already exists
        if (userDAO.emailExists(email)) {
            showMessage("Email already registered. Please login instead.", "error");
            return;
        }

        if (sendOtpButton != null) {
            sendOtpButton.setDisable(true);
        }
        showMessage("Sending OTP...", "info");

        Task<Boolean> otpTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                generatedOtp = OTPGenerator.generateOTP();
                return emailSender.sendOTP(email, generatedOtp);
            }
        };

        otpTask.setOnSucceeded(e -> {
            if (otpTask.getValue()) {
                showMessage("OTP sent to your email. Please check and enter below.", "success");
                if (otpField != null) {
                    otpField.setDisable(false);
                    otpField.requestFocus();
                }
                if (verifyOtpButton != null) {
                    verifyOtpButton.setDisable(false);
                }
            } else {
                showMessage("Failed to send OTP. Please try again.", "error");
                if (sendOtpButton != null) {
                    sendOtpButton.setDisable(false);
                }
            }
        });

        otpTask.setOnFailed(e -> {
            showMessage("Error sending OTP. Please check your connection.", "error");
            if (sendOtpButton != null) {
                sendOtpButton.setDisable(false);
            }
        });

        Thread otpThread = new Thread(otpTask);
        otpThread.setDaemon(true);
        otpThread.start();
    }

    @FXML
    private void handleVerifyOtp(ActionEvent event) {
        if (otpField == null) return;

        String enteredOtp = otpField.getText().trim();

        if (enteredOtp.isEmpty()) {
            showMessage("Please enter the OTP", "error");
            return;
        }

        if (generatedOtp != null && generatedOtp.equals(enteredOtp)) {
            otpVerified = true;
            showMessage("OTP verified successfully!", "success");
            if (otpField != null) {
                otpField.setDisable(true);
            }
            if (verifyOtpButton != null) {
                verifyOtpButton.setDisable(true);
            }
            if (signupButton != null) {
                signupButton.setDisable(false);
            }
        } else {
            showMessage("Invalid OTP. Please try again.", "error");
            if (otpField != null) {
                otpField.clear();
                otpField.requestFocus();
            }
        }
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        if (!otpVerified) {
            showMessage("Please verify your email first", "error");
            return;
        }

        if (!validateInput()) {
            return;
        }

        setButtonsDisabled(true);
        showMessage("Creating account...", "info");

        Task<Boolean> signupTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                User user = createUserFromInput();
                return userDAO.registerUser(user);
            }
        };

        signupTask.setOnSucceeded(e -> {
            setButtonsDisabled(false);
            if (signupTask.getValue()) {
                showMessage("Account created successfully! Please login.", "success");
                clearForm();
                // Navigate to login after 2 seconds
                Task<Void> delayTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Thread.sleep(2000);
                        return null;
                    }
                };
                delayTask.setOnSucceeded(ev -> {
                    if (loginManager != null) {
                        loginManager.showLoginScreen();
                    }
                });
                new Thread(delayTask).start();
            } else {
                showMessage("Registration failed. Please try again.", "error");
            }
        });

        signupTask.setOnFailed(e -> {
            setButtonsDisabled(false);
            showMessage("Registration failed. Please check your connection.", "error");
        });

        Thread signupThread = new Thread(signupTask);
        signupThread.setDaemon(true);
        signupThread.start();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        if (loginManager != null) {
            loginManager.showLoginScreen();
        }
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (event.getSource() == otpField && verifyOtpButton != null && !verifyOtpButton.isDisabled()) {
                handleVerifyOtp(null);
            } else if (signupButton != null && !signupButton.isDisabled()) {
                handleSignup(null);
            }
        }
    }

    private boolean validateInput() {
        String email = emailField != null ? emailField.getText().trim() : "";
        String fullName = fullNameField != null ? fullNameField.getText().trim() : "";
        String phone = phoneField != null ? phoneField.getText().trim() : "";
        String userType = userTypeCombo != null ? userTypeCombo.getValue() : null;
        String password = passwordField != null ? passwordField.getText() : "";
        String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText() : "";
        String faculty = facultyCombo != null ? facultyCombo.getValue() : null;

        // Check basic required fields (username is optional since it's not in FXML)
        if (email.isEmpty() || fullName.isEmpty() || phone.isEmpty() ||
                userType == null || password.isEmpty() || confirmPassword.isEmpty() || faculty == null) {
            showMessage("Please fill in all required fields", "error");
            return false;
        }

        if (!isValidEmail(email)) {
            showMessage("Please enter a valid email address", "error");
            if (emailField != null) {
                emailField.requestFocus();
            }
            return false;
        }

        // Only validate username if the field exists and has content
        if (usernameField != null && !usernameField.getText().trim().isEmpty()) {
            String username = usernameField.getText().trim();

            if (!isValidUsername(username)) {
                showMessage("Username can only contain letters, numbers, and underscores (3-20 characters)", "error");
                usernameField.requestFocus();
                return false;
            }

            // Check if username already exists
            if (userDAO.usernameExists(username)) {
                showMessage("Username already taken. Please choose another.", "error");
                usernameField.requestFocus();
                return false;
            }
        }

        if (fullName.length() < 2) {
            showMessage("Full name must be at least 2 characters", "error");
            if (fullNameField != null) {
                fullNameField.requestFocus();
            }
            return false;
        }

        if (!phone.matches("\\d{10}")) {
            showMessage("Phone number must be 10 digits", "error");
            if (phoneField != null) {
                phoneField.requestFocus();
            }
            return false;
        }

        if (password.length() < 6) {
            showMessage("Password must be at least 6 characters", "error");
            if (passwordField != null) {
                passwordField.requestFocus();
            }
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match", "error");
            if (confirmPasswordField != null) {
                confirmPasswordField.requestFocus();
            }
            return false;
        }

        // Validate student-specific fields
        if ("STUDENT".equals(userType)) {
            String rollNo = rollNoField != null ? rollNoField.getText().trim() : "";
            Integer semester = semesterCombo != null ? semesterCombo.getValue() : null;

            if (rollNo.isEmpty()) {
                showMessage("Roll number is required for students", "error");
                if (rollNoField != null) {
                    rollNoField.requestFocus();
                }
                return false;
            }

            if (semester == null) {
                showMessage("Semester is required for students", "error");
                if (semesterCombo != null) {
                    semesterCombo.requestFocus();
                }
                return false;
            }
        }

        return true;
    }

    private User createUserFromInput() {
        String userType = userTypeCombo != null ? userTypeCombo.getValue() : "STUDENT";
        String rollNo = "STUDENT".equals(userType) && rollNoField != null ? rollNoField.getText().trim() : null;
        Integer semester = "STUDENT".equals(userType) && semesterCombo != null ? semesterCombo.getValue() : null;

        // Create user using the backward compatible constructor
        User user = new User(
                emailField != null ? emailField.getText().trim() : "",
                fullNameField != null ? fullNameField.getText().trim() : "",
                phoneField != null ? phoneField.getText().trim() : "",
                userType,
                rollNo,
                semester,
                facultyCombo != null ? facultyCombo.getValue() : "",
                passwordField != null ? passwordField.getText() : ""
        );

        // Set username - use email prefix if no separate username field
        String username;
        if (usernameField != null && !usernameField.getText().trim().isEmpty()) {
            username = usernameField.getText().trim();
        } else {
            // Use the part before @ in email as username
            String email = emailField != null ? emailField.getText().trim() : "";
            if (!email.isEmpty() && email.contains("@")) {
                username = email.substring(0, email.indexOf("@"));
            } else {
                username = email; // fallback to full email
            }
        }

        user.setUsername(username);
        return user;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidUsername(String username) {
        // Username should be 3-20 characters, only letters, numbers, and underscores
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    private void setButtonsDisabled(boolean disabled) {
        if (sendOtpButton != null) {
            sendOtpButton.setDisable(disabled);
        }
        if (verifyOtpButton != null) {
            verifyOtpButton.setDisable(disabled);
        }
        if (signupButton != null) {
            signupButton.setDisable(disabled);
        }
        if (loginButton != null) {
            loginButton.setDisable(disabled);
        }

        if (emailField != null) {
            emailField.setDisable(disabled);
        }
        if (usernameField != null) {
            usernameField.setDisable(disabled);
        }
        if (fullNameField != null) {
            fullNameField.setDisable(disabled);
        }
        if (phoneField != null) {
            phoneField.setDisable(disabled);
        }
        if (userTypeCombo != null) {
            userTypeCombo.setDisable(disabled);
        }
        if (rollNoField != null) {
            rollNoField.setDisable(disabled);
        }
        if (semesterCombo != null) {
            semesterCombo.setDisable(disabled);
        }
        if (facultyCombo != null) {
            facultyCombo.setDisable(disabled);
        }
        if (passwordField != null) {
            passwordField.setDisable(disabled);
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.setDisable(disabled);
        }
        if (otpField != null) {
            otpField.setDisable(disabled || !otpVerified);
        }
    }

    private void resetOtpFields() {
        if (otpField != null) {
            otpField.clear();
            otpField.setDisable(true);
        }
        if (verifyOtpButton != null) {
            verifyOtpButton.setDisable(true);
        }
        if (signupButton != null) {
            signupButton.setDisable(true);
        }
        generatedOtp = null;
    }

    private void showMessage(String message, String type) {
        if (messageLabel == null) return;

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

    private void clearForm() {
        if (emailField != null) {
            emailField.clear();
        }
        if (usernameField != null) {
            usernameField.clear();
        }
        if (fullNameField != null) {
            fullNameField.clear();
        }
        if (phoneField != null) {
            phoneField.clear();
        }
        if (rollNoField != null) {
            rollNoField.clear();
        }
        if (passwordField != null) {
            passwordField.clear();
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.clear();
        }
        if (otpField != null) {
            otpField.clear();
        }
        if (userTypeCombo != null) {
            userTypeCombo.setValue("STUDENT");
        }
        if (semesterCombo != null) {
            semesterCombo.setValue(null);
        }
        if (facultyCombo != null) {
            facultyCombo.setValue(null);
        }
        if (messageLabel != null) {
            messageLabel.setText("");
        }
        otpVerified = false;
        resetOtpFields();
    }
}