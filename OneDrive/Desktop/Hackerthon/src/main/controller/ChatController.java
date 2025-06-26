package main.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import main.db.DatabaseConnection;
import main.model.Message;
import main.model.User;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class ChatController implements Initializable {

    @FXML private ComboBox<String> chatTypeComboBox;
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatMessagesVBox;
    @FXML private TextArea messageTextArea;
    @FXML private Button sendButton;
    @FXML private ListView<String> onlineUsersListView;
    @FXML private Label chatTitleLabel;

    private String currentUsername;
    private String currentChatType = "General";
    private Timer refreshTimer;
    private ObservableList<String> onlineUsers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupChatTypes();
        setupOnlineUsers();
        setupMessageArea();
        startAutoRefresh();
    }

    public void setCurrentUser(String username) {
        this.currentUsername = username;
        updateUserOnlineStatus(true);
        loadMessages();
        loadOnlineUsers();
    }

    private void setupChatTypes() {
        chatTypeComboBox.getItems().addAll("General", "Study Group", "Academic", "Sports", "Events");
        chatTypeComboBox.setValue("General");
        chatTypeComboBox.setOnAction(e -> {
            currentChatType = chatTypeComboBox.getValue();
            chatTitleLabel.setText(currentChatType + " Chat");
            loadMessages();
        });
    }

    private void setupOnlineUsers() {
        onlineUsers = FXCollections.observableArrayList();
        onlineUsersListView.setItems(onlineUsers);
    }

    private void setupMessageArea() {
        messageTextArea.setPromptText("Type your message here...");
        messageTextArea.setWrapText(true);

        // Send message on Enter key
        messageTextArea.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER") && !e.isShiftDown()) {
                e.consume();
                handleSendMessage();
            }
        });

        sendButton.setOnAction(e -> handleSendMessage());
    }

    @FXML
    private void handleSendMessage() {
        String messageText = messageTextArea.getText().trim();
        if (messageText.isEmpty()) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO messages (sender_username, chat_type, message_text, timestamp) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUsername);
            stmt.setString(2, currentChatType);
            stmt.setString(3, messageText);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            stmt.executeUpdate();
            messageTextArea.clear();
            loadMessages(); // Refresh messages

        } catch (SQLException e) {
            showAlert("Error", "Failed to send message: " + e.getMessage());
        }
    }

    private void loadMessages() {
        Platform.runLater(() -> {
            chatMessagesVBox.getChildren().clear();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT m.*, u.full_name FROM messages m " +
                        "JOIN users u ON m.sender_username = u.username " +
                        "WHERE m.chat_type = ? " +
                        "ORDER BY m.timestamp ASC LIMIT 50";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, currentChatType);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    addMessageToChat(
                            rs.getString("sender_username"),
                            rs.getString("full_name"),
                            rs.getString("message_text"),
                            rs.getTimestamp("timestamp").toLocalDateTime()
                    );
                }

                // Scroll to bottom
                Platform.runLater(() -> chatScrollPane.setVvalue(1.0));

            } catch (SQLException e) {
                showAlert("Error", "Failed to load messages: " + e.getMessage());
            }
        });
    }

    private void addMessageToChat(String username, String fullName, String messageText, LocalDateTime timestamp) {
        VBox messageContainer = new VBox(5);
        messageContainer.setPadding(new Insets(10));

        // Message header
        HBox headerBox = new HBox(10);
        Label usernameLabel = new Label(fullName + " (@" + username + ")");
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");

        Label timeLabel = new Label(timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")));
        timeLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");

        headerBox.getChildren().addAll(usernameLabel, timeLabel);

        // Message content
        Label messageLabel = new Label(messageText);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-padding: 5 0 0 0;");

        messageContainer.getChildren().addAll(headerBox, messageLabel);

        // Different styling for own messages
        if (username.equals(currentUsername)) {
            messageContainer.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 10;");
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
        } else {
            messageContainer.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10;");
        }

        chatMessagesVBox.getChildren().add(messageContainer);
    }

    private void loadOnlineUsers() {
        Platform.runLater(() -> {
            onlineUsers.clear();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT username, full_name FROM users WHERE is_online = true ORDER BY full_name";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String displayName = rs.getString("full_name") + " (@" + rs.getString("username") + ")";
                    onlineUsers.add(displayName);
                }

            } catch (SQLException e) {
                System.err.println("Failed to load online users: " + e.getMessage());
            }
        });
    }

    private void updateUserOnlineStatus(boolean isOnline) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users SET is_online = ?, last_seen = ? WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, isOnline);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, currentUsername);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to update online status: " + e.getMessage());
        }
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                loadMessages();
                loadOnlineUsers();
            }
        }, 5000, 5000); // Refresh every 5 seconds
    }

    public void cleanup() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
        if (currentUsername != null) {
            updateUserOnlineStatus(false);
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}