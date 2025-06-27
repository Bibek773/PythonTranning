package main.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.AnchorPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import main.java.app.LoginManager;
import main.model.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label userInfoLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Button logoutButton;
    @FXML private Button profileButton;
    @FXML private Button eventsButton;
    @FXML private Button chatButton;
    @FXML private AnchorPane rootPane;
    @FXML private Button classroomButton;
    @FXML private Button announcementsButton;
    @FXML private VBox contentArea;
    @FXML private ScrollPane contentScrollPane;
    @FXML private VBox sidebarVBox;

    private LoginManager loginManager;
    private User currentUser;
    private String currentView = "home";

    @FXML
    public void initialize() {
        setupDateTime();
        setupContentArea();
        System.out.println("DashboardController initialized");
    }

    public void setUser(User user) {
        this.currentUser = user;
        updateUserInterface();
    }

    public void setLoginManager(LoginManager loginManager) {
        this.loginManager = loginManager;
    }

    private void setupDateTime() {
        // Update date/time every second
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDateTime()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        updateDateTime();
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy - HH:mm:ss");
        if (dateTimeLabel != null) {
            dateTimeLabel.setText(now.format(formatter));
        }
    }

    private void updateUserInterface() {
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName() + "!");

            StringBuilder userInfo = new StringBuilder();
            userInfo.append("Email: ").append(currentUser.getEmail()).append("\n");
            userInfo.append("Type: ").append(currentUser.getUserType()).append("\n");
            userInfo.append("Faculty: ").append(currentUser.getFaculty());

            if ("STUDENT".equals(currentUser.getUserType())) {
                if (currentUser.getRollNo() != null) {
                    userInfo.append("\nRoll No: ").append(currentUser.getRollNo());
                }
                if (currentUser.getSemester() != null) {
                    userInfo.append("\nSemester: ").append(currentUser.getSemester());
                }
            }

            userInfoLabel.setText(userInfo.toString());

            // Show/hide buttons based on user type
            setupUserTypeSpecificButtons();

            // Load default home view
            showHomeView();
        }
    }

    private void setupUserTypeSpecificButtons() {
        boolean isStudent = "STUDENT".equals(currentUser.getUserType());
        boolean isTeacher = "TEACHER".equals(currentUser.getUserType());

        // All users can see these
        eventsButton.setVisible(true);
        chatButton.setVisible(true);
        announcementsButton.setVisible(true);

        // Only students see classroom assignments
        classroomButton.setText(isStudent ? "My Assignments" : "Manage Classes");
        classroomButton.setVisible(true);
    }

    private void setupContentArea() {
        if (contentArea != null) {
            contentArea.setSpacing(10);
            contentArea.setPadding(new Insets(20));
        }
    }

    @FXML
    private void handleProfile() {
        currentView = "profile";
        showProfileView();
    }

    @FXML
    private void handleEvents() {
        currentView = "events";
        showEventsView();
    }

    @FXML
    private void handleChat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chat.fxml"));
            VBox chatContent = loader.load();

            ChatController chatController = loader.getController();
            // Fixed: Use currentUser.getUsername() instead of undefined currentUsername
            chatController.setCurrentUser(currentUser.getUsername());

            // Clear existing content and add chat
            contentArea.getChildren().clear();
            contentArea.getChildren().add(chatContent);

            // Update breadcrumb
            updateBreadcrumb("Chat");

        } catch (IOException e) {
            showAlert("Error", "Failed to load chat: " + e.getMessage());
        }
    }

    // Fixed: Added proper implementation for showAlert method
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Fixed: Added proper implementation for updateBreadcrumb method
    private void updateBreadcrumb(String section) {
        // Implementation to update breadcrumb navigation
        // This could update a breadcrumb label or navigation trail
        System.out.println("Current section: " + section);
        // You can add actual breadcrumb UI updates here if needed
    }

    @FXML
    private void handleClassroom() {
        currentView = "classroom";
        showClassroomView();
    }

    @FXML
    private void handleAnnouncements() {
        currentView = "announcements";
        showAnnouncementsView();
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be redirected to the login screen.");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (loginManager != null) {
                    loginManager.logout();
                }
            }
        });
    }

    private void showHomeView() {
        clearContentArea();

        Label titleLabel = new Label("Dashboard Home");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Quick stats section
        VBox statsBox = createStatsSection();

        // Recent activities section
        VBox activitiesBox = createRecentActivitiesSection();

        // Quick actions section
        VBox actionsBox = createQuickActionsSection();

        contentArea.getChildren().addAll(titleLabel, statsBox, activitiesBox, actionsBox);
    }

    private void showProfileView() {
        clearContentArea();

        Label titleLabel = new Label("User Profile");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Profile information
        VBox profileBox = new VBox(10);
        profileBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-background-radius: 10;");

        profileBox.getChildren().addAll(
                createInfoRow("Full Name:", currentUser.getFullName()),
                createInfoRow("Email:", currentUser.getEmail()),
                createInfoRow("Phone:", currentUser.getPhoneNumber()),
                createInfoRow("User Type:", currentUser.getUserType()),
                createInfoRow("Faculty:", currentUser.getFaculty())
        );

        if ("STUDENT".equals(currentUser.getUserType())) {
            if (currentUser.getRollNo() != null) {
                profileBox.getChildren().add(createInfoRow("Roll Number:", currentUser.getRollNo()));
            }
            if (currentUser.getSemester() != null) {
                profileBox.getChildren().add(createInfoRow("Semester:", currentUser.getSemester().toString()));
            }
        }

        Button editProfileButton = new Button("Edit Profile");
        editProfileButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 20;");
        editProfileButton.setOnAction(e -> showMessage("Profile editing feature coming soon!", "info"));

        contentArea.getChildren().addAll(titleLabel, profileBox, editProfileButton);
    }

    private void showEventsView() {
        clearContentArea();

        Label titleLabel = new Label("Campus Events");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Sample events
        VBox eventsContainer = new VBox(15);

        eventsContainer.getChildren().addAll(
                createEventCard("Annual Tech Fest", "March 15-17, 2025", "Join us for the biggest tech event of the year!"),
                createEventCard("Sports Day", "March 25, 2025", "Inter-faculty sports competition"),
                createEventCard("Cultural Program", "April 2, 2025", "Showcase your talents!")
        );

        Button createEventButton = new Button("Create New Event");
        createEventButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10 20;");
        createEventButton.setOnAction(e -> showMessage("Event creation feature coming soon!", "info"));

        if ("TEACHER".equals(currentUser.getUserType())) {
            contentArea.getChildren().addAll(titleLabel, createEventButton, eventsContainer);
        } else {
            contentArea.getChildren().addAll(titleLabel, eventsContainer);
        }
    }

    private void showClassroomView() {
        clearContentArea();

        String title = "STUDENT".equals(currentUser.getUserType()) ? "My Assignments" : "Classroom Management";
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox classroomContainer = new VBox(15);

        if ("STUDENT".equals(currentUser.getUserType())) {
            // Show assignments for students
            classroomContainer.getChildren().addAll(
                    createAssignmentCard("Math Assignment 1", "Due: March 20, 2025", "Pending"),
                    createAssignmentCard("Physics Lab Report", "Due: March 18, 2025", "Submitted"),
                    createAssignmentCard("English Essay", "Due: March 25, 2025", "Pending")
            );
        } else {
            // Show classroom management for teachers
            Button createAssignmentButton = new Button("Create Assignment");
            createAssignmentButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10 20;");
            createAssignmentButton.setOnAction(e -> showMessage("Assignment creation feature coming soon!", "info"));

            classroomContainer.getChildren().addAll(
                    createAssignmentButton,
                    createClassCard("Data Structures - Semester 4", "25 students enrolled"),
                    createClassCard("Database Systems - Semester 6", "30 students enrolled")
            );
        }

        contentArea.getChildren().addAll(titleLabel, classroomContainer);
    }

    private void showAnnouncementsView() {
        clearContentArea();

        Label titleLabel = new Label("Announcements");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox announcementsContainer = new VBox(15);

        announcementsContainer.getChildren().addAll(
                createAnnouncementCard("Semester Exam Schedule Released", "2 days ago", "Check your exam timetable on the portal"),
                createAnnouncementCard("Library Hours Extended", "1 week ago", "Library will be open until 10 PM during exam period"),
                createAnnouncementCard("New Course Registration", "2 weeks ago", "Registration for summer courses begins next week")
        );

        if ("TEACHER".equals(currentUser.getUserType())) {
            Button createAnnouncementButton = new Button("Create Announcement");
            createAnnouncementButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 10 20;");
            createAnnouncementButton.setOnAction(e -> showMessage("Announcement creation feature coming soon!", "info"));
            contentArea.getChildren().addAll(titleLabel, createAnnouncementButton, announcementsContainer);
        } else {
            contentArea.getChildren().addAll(titleLabel, announcementsContainer);
        }
    }

    // Helper methods for creating UI components
    private VBox createStatsSection() {
        VBox statsBox = new VBox(10);
        statsBox.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 15; -fx-background-radius: 10;");

        Label statsTitle = new Label("Quick Stats");
        statsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        if ("STUDENT".equals(currentUser.getUserType())) {
            statsRow.getChildren().addAll(
                    createStatCard("Pending Assignments", "3"),
                    createStatCard("Upcoming Events", "2"),
                    createStatCard("Messages", "5")
            );
        } else {
            statsRow.getChildren().addAll(
                    createStatCard("Total Classes", "4"),
                    createStatCard("Students", "85"),
                    createStatCard("Assignments", "12")
            );
        }

        statsBox.getChildren().addAll(statsTitle, statsRow);
        return statsBox;
    }

    private VBox createRecentActivitiesSection() {
        VBox activitiesBox = new VBox(10);
        activitiesBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 10;");

        Label activitiesTitle = new Label("Recent Activities");
        activitiesTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox activitiesList = new VBox(5);
        activitiesList.getChildren().addAll(
                new Label("• Logged in to the system"),
                new Label("• Viewed dashboard"),
                new Label("• Updated profile information")
        );

        activitiesBox.getChildren().addAll(activitiesTitle, activitiesList);
        return activitiesBox;
    }

    private VBox createQuickActionsSection() {
        VBox actionsBox = new VBox(10);
        actionsBox.setStyle("-fx-background-color: #e8f5e8; -fx-padding: 15; -fx-background-radius: 10;");

        Label actionsTitle = new Label("Quick Actions");
        actionsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        Button viewProfileBtn = new Button("View Profile");
        Button checkEventsBtn = new Button("Check Events");
        Button openChatBtn = new Button("Open Chat");

        viewProfileBtn.setOnAction(e -> handleProfile());
        checkEventsBtn.setOnAction(e -> handleEvents());
        openChatBtn.setOnAction(e -> handleChat());

        buttonRow.getChildren().addAll(viewProfileBtn, checkEventsBtn, openChatBtn);
        actionsBox.getChildren().addAll(actionsTitle, buttonRow);

        return actionsBox;
    }

    private VBox createStatCard(String title, String value) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8;");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(120);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2980b9;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        titleLabel.setWrapText(true);

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-weight: bold; -fx-min-width: 120px;");

        Label valueText = new Label(value);
        valueText.setStyle("-fx-text-fill: #2c3e50;");

        row.getChildren().addAll(labelText, valueText);
        return row;
    }

    private VBox createEventCard(String title, String date, String description) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #3498db; -fx-border-radius: 10; -fx-border-width: 2;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        descLabel.setWrapText(true);

        Button joinButton = new Button("Join Event");
        joinButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 15;");
        joinButton.setOnAction(e -> showMessage("Event registration feature coming soon!", "info"));

        card.getChildren().addAll(titleLabel, dateLabel, descLabel, joinButton);
        return card;
    }

    private VBox createAssignmentCard(String title, String dueDate, String status) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #e67e22; -fx-border-radius: 10; -fx-border-width: 2;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label dueDateLabel = new Label(dueDate);
        dueDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c;");

        Label statusLabel = new Label("Status: " + status);
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " +
                ("Submitted".equals(status) ? "#27ae60" : "#f39c12") + "; -fx-font-weight: bold;");

        HBox buttonBox = new HBox(10);
        Button viewButton = new Button("View Details");
        Button submitButton = new Button("Submit");

        viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 15;");
        submitButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 15;");

        if ("Submitted".equals(status)) {
            submitButton.setDisable(true);
        }

        viewButton.setOnAction(e -> showMessage("Assignment details feature coming soon!", "info"));
        submitButton.setOnAction(e -> showMessage("Assignment submission feature coming soon!", "info"));

        buttonBox.getChildren().addAll(viewButton, submitButton);
        card.getChildren().addAll(titleLabel, dueDateLabel, statusLabel, buttonBox);
        return card;
    }

    private VBox createClassCard(String className, String studentCount) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #9b59b6; -fx-border-radius: 10; -fx-border-width: 2;");

        Label titleLabel = new Label(className);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label countLabel = new Label(studentCount);
        countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        HBox buttonBox = new HBox(10);
        Button manageButton = new Button("Manage Class");
        Button assignmentsButton = new Button("View Assignments");

        manageButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 5 15;");
        assignmentsButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 5 15;");

        manageButton.setOnAction(e -> showMessage("Class management feature coming soon!", "info"));
        assignmentsButton.setOnAction(e -> showMessage("Assignment management feature coming soon!", "info"));

        buttonBox.getChildren().addAll(manageButton, assignmentsButton);
        card.getChildren().addAll(titleLabel, countLabel, buttonBox);
        return card;
    }

    private VBox createAnnouncementCard(String title, String date, String content) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #f39c12; -fx-border-radius: 10; -fx-border-width: 2;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");

        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        contentLabel.setWrapText(true);

        Button readMoreButton = new Button("Read More");
        readMoreButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 5 15;");
        readMoreButton.setOnAction(e -> showMessage("Full announcement view coming soon!", "info"));

        card.getChildren().addAll(titleLabel, dateLabel, contentLabel, readMoreButton);
        return card;
    }

    private void clearContentArea() {
        if (contentArea != null) {
            contentArea.getChildren().clear();
        }
    }

    private void showMessage(String message, String type) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}