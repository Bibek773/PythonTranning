package main.model;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private String senderUsername;
    private String receiverUsername;
    private String chatType;
    private String messageText;
    private String messageType;
    private String filePath;
    private boolean isEdited;
    private LocalDateTime editedAt;
    private LocalDateTime timestamp;

    // Default constructor
    public Message() {}

    // Constructor for group/public chat messages (3 parameters)
    public Message(String senderUsername, String chatType, String messageText) {
        this.senderUsername = senderUsername;
        this.chatType = chatType;
        this.messageText = messageText;
        this.messageType = "text";
        this.timestamp = LocalDateTime.now();
        this.isEdited = false;
        this.receiverUsername = null; // No specific receiver for group messages
    }

    // Constructor for private messages (4 parameters - added chatType parameter)
    public Message(String senderUsername, String receiverUsername, String chatType, String messageText) {
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.chatType = chatType;
        this.messageText = messageText;
        this.messageType = "text";
        this.timestamp = LocalDateTime.now();
        this.isEdited = false;
    }

    // Alternative constructor for private messages (using builder pattern style)
    public static Message createPrivateMessage(String senderUsername, String receiverUsername, String messageText) {
        Message message = new Message();
        message.senderUsername = senderUsername;
        message.receiverUsername = receiverUsername;
        message.messageText = messageText;
        message.chatType = "Private";
        message.messageType = "text";
        message.timestamp = LocalDateTime.now();
        message.isEdited = false;
        return message;
    }

    // Full constructor
    public Message(int id, String senderUsername, String receiverUsername, String chatType,
                   String messageText, String messageType, String filePath, boolean isEdited,
                   LocalDateTime editedAt, LocalDateTime timestamp) {
        this.id = id;
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.chatType = chatType;
        this.messageText = messageText;
        this.messageType = messageType;
        this.filePath = filePath;
        this.isEdited = isEdited;
        this.editedAt = editedAt;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getReceiverUsername() { return receiverUsername; }
    public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }

    public String getChatType() { return chatType; }
    public void setChatType(String chatType) { this.chatType = chatType; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public boolean isEdited() { return isEdited; }
    public void setEdited(boolean edited) { isEdited = edited; }

    public LocalDateTime getEditedAt() { return editedAt; }
    public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s",
                timestamp != null ? timestamp.toString() : "Unknown",
                senderUsername,
                messageText);
    }
}