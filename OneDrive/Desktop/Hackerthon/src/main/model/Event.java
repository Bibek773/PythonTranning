package main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Event {
    private int eventId;
    private String eventName;
    private String description;
    private String eventType; // ACADEMIC, CULTURAL, SPORTS, TECHNICAL, SEMINAR
    private String location;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int maxParticipants;
    private int currentParticipants;
    private String organizerUsername;
    private String organizerName;
    private String faculty;
    private String status; // UPCOMING, ONGOING, COMPLETED, CANCELLED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String requirements; // Any special requirements or eligibility
    private String contactInfo;
    private boolean isRegistrationOpen;

    // Constructors
    public Event() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "UPCOMING";
        this.currentParticipants = 0;
        this.isRegistrationOpen = true;
    }

    public Event(String eventName, String description, String eventType, String location,
                 LocalDate eventDate, LocalTime startTime, LocalTime endTime,
                 int maxParticipants, String organizerUsername, String organizerName,
                 String faculty) {
        this();
        this.eventName = eventName;
        this.description = description;
        this.eventType = eventType;
        this.location = location;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxParticipants = maxParticipants;
        this.organizerUsername = organizerUsername;
        this.organizerName = organizerName;
        this.faculty = faculty;
    }

    // Getters and Setters
    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public int getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(int currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public String getOrganizerUsername() {
        return organizerUsername;
    }

    public void setOrganizerUsername(String organizerUsername) {
        this.organizerUsername = organizerUsername;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public boolean isRegistrationOpen() {
        return isRegistrationOpen;
    }

    public void setRegistrationOpen(boolean registrationOpen) {
        isRegistrationOpen = registrationOpen;
    }

    // Utility methods
    public boolean isFull() {
        return currentParticipants >= maxParticipants;
    }

    public boolean canRegister() {
        return isRegistrationOpen && !isFull() && "UPCOMING".equals(status);
    }

    public int getAvailableSlots() {
        return maxParticipants - currentParticipants;
    }

    public String getFormattedDateTime() {
        return eventDate.toString() + " " + startTime.toString() + " - " + endTime.toString();
    }

    public String getFormattedDate() {
        return eventDate.toString();
    }

    public String getFormattedTime() {
        return startTime.toString() + " - " + endTime.toString();
    }

    // Validation methods
    public boolean isValid() {
        return eventName != null && !eventName.trim().isEmpty() &&
                description != null && !description.trim().isEmpty() &&
                eventType != null && !eventType.trim().isEmpty() &&
                location != null && !location.trim().isEmpty() &&
                eventDate != null && startTime != null && endTime != null &&
                maxParticipants > 0 &&
                organizerUsername != null && !organizerUsername.trim().isEmpty();
    }

    public boolean isTimeValid() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    public boolean isDateValid() {
        return eventDate != null && eventDate.isAfter(LocalDate.now().minusDays(1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return eventId == event.eventId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", eventName='" + eventName + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventDate=" + eventDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", location='" + location + '\'' +
                ", currentParticipants=" + currentParticipants +
                ", maxParticipants=" + maxParticipants +
                ", status='" + status + '\'' +
                '}';
    }
}