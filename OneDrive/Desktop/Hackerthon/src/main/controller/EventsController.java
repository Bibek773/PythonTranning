package main.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

import main.model.Event;
import main.model.User;
import main.db.EventDAO;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EventsController {
    @FXML private VBox contentArea;
    @FXML private ScrollPane contentScrollPane;
    @FXML private Button createEventButton;
    @FXML private Button myEventsButton;
    @FXML private Button allEventsButton;
    @FXML private Button registeredEventsButton;
    @FXML private ComboBox<String> eventTypeFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;

    private User currentUser;
    private EventDAO eventDAO;
    private String currentView = "all";
    private List<Event> currentEventList;

    @FXML
    public void initialize() {
        eventDAO = new EventDAO();
        setupFilters();
        setupContentArea();
        System.out.println("EventController initialized");
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUserInterface();
        loadAllEvents();
    }

    private void setupFilters() {
        if (eventTypeFilter != null) {
            ObservableList<String> eventTypes = FXCollections.observableArrayList(
                    "All Types", "ACADEMIC", "CULTURAL", "SPORTS", "TECHNICAL", "SEMINAR", "WORKSHOP"
            );
            eventTypeFilter.setItems(eventTypes);
            eventTypeFilter.setValue("All Types");
            eventTypeFilter.setOnAction(e -> applyFilters());
        }

        if (statusFilter != null) {
            ObservableList<String> statuses = FXCollections.observableArrayList(
                    "All Status", "UPCOMING", "ONGOING", "COMPLETED", "CANCELLED"
            );
            statusFilter.setItems(statuses);
            statusFilter.setValue("All Status");
            statusFilter.setOnAction(e -> applyFilters());
        }
    }

    private void setupContentArea() {
        if (contentArea != null) {
            contentArea.setSpacing(15);
            contentArea.setPadding(new Insets(20));
        }
    }

    private void updateUserInterface() {
        if (currentUser != null) {
            boolean isTeacher = "TEACHER".equals(currentUser.getUserType());

            if (createEventButton != null) {
                createEventButton.setVisible(isTeacher);
            }

            if (myEventsButton != null) {
                myEventsButton.setText(isTeacher ? "My Created Events" : "My Registered Events");
            }
        }
    }

    @FXML
    private void handleCreateEvent() {
        showCreateEventDialog();
    }

    @FXML
    private void handleMyEvents() {
        currentView = "my";
        if ("TEACHER".equals(currentUser.getUserType())) {
            loadMyCreatedEvents();
        } else {
            loadMyRegisteredEvents();
        }
    }

    @FXML
    private void handleAllEvents() {
        currentView = "all";
        loadAllEvents();
    }

    @FXML
    private void handleRegisteredEvents() {
        currentView = "registered";
        loadMyRegisteredEvents();
    }

    @FXML
    private void handleSearch() {
        performSearch();
    }

    @FXML
    private void handleRefresh() {
        refreshCurrentView();
    }

    private void loadAllEvents() {
        currentEventList = eventDAO.getAllEvents();
        displayEvents(currentEventList, "All Events");
    }

    private void loadMyCreatedEvents() {
        currentEventList = eventDAO.getEventsByOrganizer(currentUser.getUsername());
        displayEvents(currentEventList, "My Created Events");
    }

    private void loadMyRegisteredEvents() {
        currentEventList = eventDAO.getUserRegisteredEvents(currentUser.getUsername());
        displayEvents(currentEventList, "My Registered Events");
    }

    private void refreshCurrentView() {
        switch (currentView) {
            case "all":
                loadAllEvents();
                break;
            case "my":
                if ("TEACHER".equals(currentUser.getUserType())) {
                    loadMyCreatedEvents();
                } else {
                    loadMyRegisteredEvents();
                }
                break;
            case "registered":
                loadMyRegisteredEvents();
                break;
            default:
                loadAllEvents();
        }
    }

    private void performSearch() {
        String searchTerm = searchField != null ? searchField.getText().trim().toLowerCase() : "";

        if (currentEventList == null) {
            refreshCurrentView();
            return;
        }

        List<Event> filteredEvents = currentEventList;

        if (!searchTerm.isEmpty()) {
            filteredEvents = currentEventList.stream()
                    .filter(event ->
                            event.getEventName().toLowerCase().contains(searchTerm) ||
                                    event.getDescription().toLowerCase().contains(searchTerm) ||
                                    event.getLocation().toLowerCase().contains(searchTerm) ||
                                    event.getOrganizerName().toLowerCase().contains(searchTerm)
                    )
                    .collect(Collectors.toList());
        }

        displayEvents(filteredEvents, "Search Results");
    }

    private void applyFilters() {
        if (currentEventList == null) {
            return;
        }

        List<Event> filteredEvents = currentEventList.stream()
                .filter(this::matchesTypeFilter)
                .filter(this::matchesStatusFilter)
                .collect(Collectors.toList());

        displayEvents(filteredEvents, "Filtered Events");
    }

    private boolean matchesTypeFilter(Event event) {
        if (eventTypeFilter == null || "All Types".equals(eventTypeFilter.getValue())) {
            return true;
        }
        return event.getEventType().equals(eventTypeFilter.getValue());
    }

    private boolean matchesStatusFilter(Event event) {
        if (statusFilter == null || "All Status".equals(statusFilter.getValue())) {
            return true;
        }
        return event.getStatus().equals(statusFilter.getValue());
    }

    private void displayEvents(List<Event> events, String title) {
        if (contentArea == null) {
            return;
        }

        contentArea.getChildren().clear();

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
        contentArea.getChildren().add(titleLabel);

        if (events.isEmpty()) {
            Label noEventsLabel = new Label("No events found.");
            noEventsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            contentArea.getChildren().add(noEventsLabel);
            return;
        }

        // Event cards
        for (Event event : events) {
            VBox eventCard = createEventCard(event);
            contentArea.getChildren().add(eventCard);
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Event name and type
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(event.getEventName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label typeLabel = new Label(event.getEventType());
        typeLabel.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-padding: 2 8; " +
                "-fx-background-radius: 12px; -fx-font-size: 12px;");

        Label statusLabel = new Label(event.getStatus());
        String statusColor = getStatusColor(event.getStatus());
        statusLabel.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; " +
                "-fx-padding: 2 8; -fx-background-radius: 12px; -fx-font-size: 12px;");

        header.getChildren().addAll(nameLabel, typeLabel, statusLabel);

        // Event details
        Label descLabel = new Label(event.getDescription());
        descLabel.setStyle("-fx-text-fill: #666; -fx-wrap-text: true;");
        descLabel.setWrapText(true);

        // Date, time, location
        HBox detailsBox = new HBox(20);
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label("📅 " + event.getFormattedDate());
        Label timeLabel = new Label("🕐 " + event.getFormattedTime());
        Label locationLabel = new Label("📍 " + event.getLocation());

        detailsBox.getChildren().addAll(dateLabel, timeLabel, locationLabel);

        // Organizer and participants
        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label organizerLabel = new Label("👤 " + event.getOrganizerName());
        Label participantsLabel = new Label("👥 " + event.getCurrentParticipants() + "/" + event.getMaxParticipants());

        infoBox.getChildren().addAll(organizerLabel, participantsLabel);

        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button viewDetailsButton = new Button("View Details");
        viewDetailsButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        viewDetailsButton.setOnAction(e -> showEventDetails(event));

        // Show different buttons based on user type and event ownership
        if ("TEACHER".equals(currentUser.getUserType()) &&
                event.getOrganizerUsername().equals(currentUser.getUsername())) {

            Button editButton = new Button("Edit");
            editButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
            editButton.setOnAction(e -> showEditEventDialog(event));

            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            deleteButton.setOnAction(e -> handleDeleteEvent(event));

            buttonBox.getChildren().addAll(viewDetailsButton, editButton, deleteButton);

        } else {
            // For students or viewing other teachers' events
            boolean isRegistered = eventDAO.isUserRegisteredForEvent(event.getEventId(), currentUser.getUsername());

            if (isRegistered) {
                Button leaveButton = new Button("Leave Event");
                leaveButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                leaveButton.setOnAction(e -> handleLeaveEvent(event));
                buttonBox.getChildren().addAll(viewDetailsButton, leaveButton);
            } else if (event.canRegister()) {
                Button joinButton = new Button("Join Event");
                joinButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
                joinButton.setOnAction(e -> handleJoinEvent(event));
                buttonBox.getChildren().addAll(viewDetailsButton, joinButton);
            } else {
                buttonBox.getChildren().add(viewDetailsButton);

                if (event.isFull()) {
                    Label fullLabel = new Label("Event Full");
                    fullLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                    buttonBox.getChildren().add(fullLabel);
                }
            }
        }

        card.getChildren().addAll(header, descLabel, detailsBox, infoBox, buttonBox);
        return card;
    }

    private String getStatusColor(String status) {
        switch (status) {
            case "UPCOMING": return "#2196f3";
            case "ONGOING": return "#4caf50";
            case "COMPLETED": return "#9e9e9e";
            case "CANCELLED": return "#f44336";
            default: return "#9e9e9e";
        }
    }

    private void showCreateEventDialog() {
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Create New Event");
        dialog.setHeaderText("Fill in the event details");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField eventName = new TextField();
        eventName.setPromptText("Event Name");

        TextArea description = new TextArea();
        description.setPromptText("Event Description");
        description.setPrefRowCount(3);

        ComboBox<String> eventType = new ComboBox<>();
        eventType.getItems().addAll("ACADEMIC", "CULTURAL", "SPORTS", "TECHNICAL", "SEMINAR", "WORKSHOP");
        eventType.setValue("ACADEMIC");

        TextField location = new TextField();
        location.setPromptText("Location");

        DatePicker eventDate = new DatePicker();
        eventDate.setValue(LocalDate.now().plusDays(1));

        TextField startTime = new TextField();
        startTime.setPromptText("HH:MM (e.g., 14:30)");

        TextField endTime = new TextField();
        endTime.setPromptText("HH:MM (e.g., 16:30)");

        TextField maxParticipants = new TextField();
        maxParticipants.setPromptText("Maximum Participants");

        TextArea requirements = new TextArea();
        requirements.setPromptText("Requirements (Optional)");
        requirements.setPrefRowCount(2);

        TextField contactInfo = new TextField();
        contactInfo.setPromptText("Contact Information");

        grid.add(new Label("Event Name:"), 0, 0);
        grid.add(eventName, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(description, 1, 1);
        grid.add(new Label("Event Type:"), 0, 2);
        grid.add(eventType, 1, 2);
        grid.add(new Label("Location:"), 0, 3);
        grid.add(location, 1, 3);
        grid.add(new Label("Event Date:"), 0, 4);
        grid.add(eventDate, 1, 4);
        grid.add(new Label("Start Time:"), 0, 5);
        grid.add(startTime, 1, 5);
        grid.add(new Label("End Time:"), 0, 6);
        grid.add(endTime, 1, 6);
        grid.add(new Label("Max Participants:"), 0, 7);
        grid.add(maxParticipants, 1, 7);
        grid.add(new Label("Requirements:"), 0, 8);
        grid.add(requirements, 1, 8);
        grid.add(new Label("Contact Info:"), 0, 9);
        grid.add(contactInfo, 1, 9);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    Event event = new Event();
                    event.setEventName(eventName.getText());
                    event.setDescription(description.getText());
                    event.setEventType(eventType.getValue());
                    event.setLocation(location.getText());
                    event.setEventDate(eventDate.getValue());
                    event.setStartTime(LocalTime.parse(startTime.getText()));
                    event.setEndTime(LocalTime.parse(endTime.getText()));
                    event.setMaxParticipants(Integer.parseInt(maxParticipants.getText()));
                    event.setOrganizerUsername(currentUser.getUsername());
                    event.setOrganizerName(currentUser.getFullName());
                    event.setFaculty(currentUser.getFaculty());
                    event.setRequirements(requirements.getText());
                    event.setContactInfo(contactInfo.getText());

                    return event;
                } catch (Exception e) {
                    showAlert("Error", "Please check your input values.", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        Optional<Event> result = dialog.showAndWait();
        result.ifPresent(event -> {
            if (event.isValid() && event.isTimeValid() && event.isDateValid()) {
                if (eventDAO.createEvent(event)) {
                    showAlert("Success", "Event created successfully!", Alert.AlertType.INFORMATION);
                    refreshCurrentView();
                } else {
                    showAlert("Error", "Failed to create event.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Error", "Please check all fields and ensure valid data.", Alert.AlertType.ERROR);
            }
        });
    }

    private void showEditEventDialog(Event event) {
        // Similar to create dialog but pre-filled with event data
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Edit Event");
        dialog.setHeaderText("Update event details");

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField eventName = new TextField(event.getEventName());
        TextArea description = new TextArea(event.getDescription());
        description.setPrefRowCount(3);

        ComboBox<String> eventType = new ComboBox<>();
        eventType.getItems().addAll("ACADEMIC", "CULTURAL", "SPORTS", "TECHNICAL", "SEMINAR", "WORKSHOP");
        eventType.setValue(event.getEventType());

        TextField location = new TextField(event.getLocation());
        DatePicker eventDate = new DatePicker(event.getEventDate());
        TextField startTime = new TextField(event.getStartTime().toString());
        TextField endTime = new TextField(event.getEndTime().toString());
        TextField maxParticipants = new TextField(String.valueOf(event.getMaxParticipants()));
        TextArea requirements = new TextArea(event.getRequirements());
        requirements.setPrefRowCount(2);
        TextField contactInfo = new TextField(event.getContactInfo());

        grid.add(new Label("Event Name:"), 0, 0);
        grid.add(eventName, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(description, 1, 1);
        grid.add(new Label("Event Type:"), 0, 2);
        grid.add(eventType, 1, 2);
        grid.add(new Label("Location:"), 0, 3);
        grid.add(location, 1, 3);
        grid.add(new Label("Event Date:"), 0, 4);
        grid.add(eventDate, 1, 4);
        grid.add(new Label("Start Time:"), 0, 5);
        grid.add(startTime, 1, 5);
        grid.add(new Label("End Time:"), 0, 6);
        grid.add(endTime, 1, 6);
        grid.add(new Label("Max Participants:"), 0, 7);
        grid.add(maxParticipants, 1, 7);
        grid.add(new Label("Requirements:"), 0, 8);
        grid.add(requirements, 1, 8);
        grid.add(new Label("Contact Info:"), 0, 9);
        grid.add(contactInfo, 1, 9);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    event.setEventName(eventName.getText());
                    event.setDescription(description.getText());
                    event.setEventType(eventType.getValue());
                    event.setLocation(location.getText());
                    event.setEventDate(eventDate.getValue());
                    event.setStartTime(LocalTime.parse(startTime.getText()));
                    event.setEndTime(LocalTime.parse(endTime.getText()));
                    event.setMaxParticipants(Integer.parseInt(maxParticipants.getText()));
                    event.setRequirements(requirements.getText());
                    event.setContactInfo(contactInfo.getText());

                    return event;
                } catch (Exception e) {
                    showAlert("Error", "Please check your input values.", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        Optional<Event> result = dialog.showAndWait();
        result.ifPresent(updatedEvent -> {
            if (updatedEvent.isValid() && updatedEvent.isTimeValid() && updatedEvent.isDateValid()) {
                if (eventDAO.updateEvent(updatedEvent)) {
                    showAlert("Success", "Event updated successfully!", Alert.AlertType.INFORMATION);
                    refreshCurrentView();
                } else {
                    showAlert("Error", "Failed to update event.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Error", "Please check all fields and ensure valid data.", Alert.AlertType.ERROR);
            }
        });
    }

    private void showEventDetails(Event event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Event Details");
        dialog.setHeaderText(event.getEventName());

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        content.getChildren().addAll(
                new Label("Description: " + event.getDescription()),
                new Label("Type: " + event.getEventType()),
                new Label("Date: " + event.getFormattedDate()),
                new Label("Time: " + event.getFormattedTime()),
                new Label("Location: " + event.getLocation()),
                new Label("Organizer: " + event.getOrganizerName()),
                new Label("Faculty: " + event.getFaculty()),
                new Label("Participants: " + event.getCurrentParticipants() + "/" + event.getMaxParticipants()),
                new Label("Status: " + event.getStatus()),
                new Label("Requirements: " + (event.getRequirements() != null ? event.getRequirements() : "None")),
                new Label("Contact: " + (event.getContactInfo() != null ? event.getContactInfo() : "N/A"))
        );

        // Show participants list if user is organizer
        if (event.getOrganizerUsername().equals(currentUser.getUsername())) {
            List<String> participants = eventDAO.getEventParticipants(event.getEventId());
            if (!participants.isEmpty()) {
                content.getChildren().add(new Label("Registered Participants:"));
                VBox participantsList = new VBox(5);
                participantsList.setPadding(new Insets(0, 0, 0, 20));
                for (String participant : participants) {
                    participantsList.getChildren().add(new Label("• " + participant));
                }
                content.getChildren().add(participantsList);
            }
        }

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void handleJoinEvent(Event event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Join Event");
        confirmAlert.setHeaderText("Join " + event.getEventName() + "?");
        confirmAlert.setContentText("Are you sure you want to register for this event?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (eventDAO.joinEvent(event.getEventId(), currentUser.getUsername(), currentUser.getFullName())) {
                showAlert("Success", "Successfully registered for the event!", Alert.AlertType.INFORMATION);
                refreshCurrentView();
            } else {
                showAlert("Error", "Failed to register for the event.", Alert.AlertType.ERROR);
            }
        }
    }

    private void handleLeaveEvent(Event event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Leave Event");
        confirmAlert.setHeaderText("Leave " + event.getEventName() + "?");
        confirmAlert.setContentText("Are you sure you want to unregister from this event?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (eventDAO.leaveEvent(event.getEventId(), currentUser.getUsername())) {
                showAlert("Success", "Successfully left the event!", Alert.AlertType.INFORMATION);
                refreshCurrentView();
            } else {
                showAlert("Error", "Failed to leave the event.", Alert.AlertType.ERROR);
            }
        }
    }

    private void handleDeleteEvent(Event event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Event");
        confirmAlert.setHeaderText("Delete " + event.getEventName() + "?");
        confirmAlert.setContentText("This action cannot be undone. All participant registrations will also be deleted.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (eventDAO.deleteEvent(event.getEventId())) {
                showAlert("Success", "Event deleted successfully!", Alert.AlertType.INFORMATION);
                refreshCurrentView();
            } else {
                showAlert("Error", "Failed to delete the event.", Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}