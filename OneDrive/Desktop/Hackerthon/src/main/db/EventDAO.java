package main.db;

import main.model.Event;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    // Create a new event
    public boolean createEvent(Event event) {
        String sql = """
            INSERT INTO events (event_name, description, event_type, location, event_date, 
                               start_time, end_time, max_participants, organizer_username, 
                               organizer_name, faculty, requirements, contact_info, 
                               registration_open, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, event.getEventName());
            pstmt.setString(2, event.getDescription());
            pstmt.setString(3, event.getEventType());
            pstmt.setString(4, event.getLocation());
            pstmt.setDate(5, Date.valueOf(event.getEventDate()));
            pstmt.setTime(6, Time.valueOf(event.getStartTime()));
            pstmt.setTime(7, Time.valueOf(event.getEndTime()));
            pstmt.setInt(8, event.getMaxParticipants());
            pstmt.setString(9, event.getOrganizerUsername());
            pstmt.setString(10, event.getOrganizerName());
            pstmt.setString(11, event.getFaculty());
            pstmt.setString(12, event.getRequirements());
            pstmt.setString(13, event.getContactInfo());
            pstmt.setBoolean(14, event.isRegistrationOpen());
            pstmt.setString(15, event.getStatus());
            pstmt.setTimestamp(16, Timestamp.valueOf(event.getCreatedAt()));
            pstmt.setTimestamp(17, Timestamp.valueOf(event.getUpdatedAt()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    event.setEventId(generatedKeys.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error creating event: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Get all events
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = """
            SELECT e.*, COUNT(ep.username) as current_participants
            FROM events e
            LEFT JOIN event_participants ep ON e.event_id = ep.event_id
            GROUP BY e.event_id
            ORDER BY e.event_date ASC, e.start_time ASC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);
                events.add(event);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching events: " + e.getMessage());
            e.printStackTrace();
        }

        return events;
    }

    // Get events by status
    public List<Event> getEventsByStatus(String status) {
        List<Event> events = new ArrayList<>();
        String sql = """
            SELECT e.*, COUNT(ep.username) as current_participants
            FROM events e
            LEFT JOIN event_participants ep ON e.event_id = ep.event_id
            WHERE e.status = ?
            GROUP BY e.event_id
            ORDER BY e.event_date ASC, e.start_time ASC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);
                events.add(event);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching events by status: " + e.getMessage());
        }

        return events;
    }

    // Get events by organizer
    public List<Event> getEventsByOrganizer(String organizerUsername) {
        List<Event> events = new ArrayList<>();
        String sql = """
            SELECT e.*, COUNT(ep.username) as current_participants
            FROM events e
            LEFT JOIN event_participants ep ON e.event_id = ep.event_id
            WHERE e.organizer_username = ?
            GROUP BY e.event_id
            ORDER BY e.event_date ASC, e.start_time ASC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, organizerUsername);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);
                events.add(event);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching events by organizer: " + e.getMessage());
        }

        return events;
    }

    // Get event by ID
    public Event getEventById(int eventId) {
        String sql = """
            SELECT e.*, COUNT(ep.username) as current_participants
            FROM events e
            LEFT JOIN event_participants ep ON e.event_id = ep.event_id
            WHERE e.event_id = ?
            GROUP BY e.event_id
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEvent(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching event by ID: " + e.getMessage());
        }

        return null;
    }

    // Join event (register participant)
    public boolean joinEvent(int eventId, String username, String userFullName) {
        // First check if event exists and can accept participants
        Event event = getEventById(eventId);
        if (event == null || !event.canRegister()) {
            return false;
        }

        // Check if user already joined
        if (isUserRegisteredForEvent(eventId, username)) {
            return false; // Already registered
        }

        String sql = "INSERT INTO event_participants (event_id, username, full_name, registered_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            pstmt.setString(2, username);
            pstmt.setString(3, userFullName);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error joining event: " + e.getMessage());
        }

        return false;
    }

    // Leave event (unregister participant)
    public boolean leaveEvent(int eventId, String username) {
        String sql = "DELETE FROM event_participants WHERE event_id = ? AND username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            pstmt.setString(2, username);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error leaving event: " + e.getMessage());
        }

        return false;
    }

    // Check if user is registered for event
    public boolean isUserRegisteredForEvent(int eventId, String username) {
        String sql = "SELECT COUNT(*) FROM event_participants WHERE event_id = ? AND username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            pstmt.setString(2, username);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error checking event registration: " + e.getMessage());
        }

        return false;
    }

    // Get events user has joined
    public List<Event> getUserRegisteredEvents(String username) {
        List<Event> events = new ArrayList<>();
        String sql = """
            SELECT e.*, COUNT(ep2.username) as current_participants
            FROM events e
            INNER JOIN event_participants ep ON e.event_id = ep.event_id
            LEFT JOIN event_participants ep2 ON e.event_id = ep2.event_id
            WHERE ep.username = ?
            GROUP BY e.event_id
            ORDER BY e.event_date ASC, e.start_time ASC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);
                events.add(event);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching user registered events: " + e.getMessage());
        }

        return events;
    }

    // Get participants of an event
    public List<String> getEventParticipants(int eventId) {
        List<String> participants = new ArrayList<>();
        String sql = "SELECT full_name FROM event_participants WHERE event_id = ? ORDER BY registered_at ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                participants.add(rs.getString("full_name"));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching event participants: " + e.getMessage());
        }

        return participants;
    }

    // Update event
    public boolean updateEvent(Event event) {
        String sql = """
            UPDATE events SET 
                event_name = ?, description = ?, event_type = ?, location = ?, 
                event_date = ?, start_time = ?, end_time = ?, max_participants = ?, 
                requirements = ?, contact_info = ?, registration_open = ?, 
                status = ?, updated_at = ?
            WHERE event_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, event.getEventName());
            pstmt.setString(2, event.getDescription());
            pstmt.setString(3, event.getEventType());
            pstmt.setString(4, event.getLocation());
            pstmt.setDate(5, Date.valueOf(event.getEventDate()));
            pstmt.setTime(6, Time.valueOf(event.getStartTime()));
            pstmt.setTime(7, Time.valueOf(event.getEndTime()));
            pstmt.setInt(8, event.getMaxParticipants());
            pstmt.setString(9, event.getRequirements());
            pstmt.setString(10, event.getContactInfo());
            pstmt.setBoolean(11, event.isRegistrationOpen());
            pstmt.setString(12, event.getStatus());
            pstmt.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(14, event.getEventId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating event: " + e.getMessage());
        }

        return false;
    }

    // Delete event
    public boolean deleteEvent(int eventId) {
        // First delete all participants
        String deleteParticipantsSql = "DELETE FROM event_participants WHERE event_id = ?";
        String deleteEventSql = "DELETE FROM events WHERE event_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Delete participants
            try (PreparedStatement pstmt1 = conn.prepareStatement(deleteParticipantsSql)) {
                pstmt1.setInt(1, eventId);
                pstmt1.executeUpdate();
            }

            // Delete event
            try (PreparedStatement pstmt2 = conn.prepareStatement(deleteEventSql)) {
                pstmt2.setInt(1, eventId);
                int affectedRows = pstmt2.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                }
            }

        } catch (SQLException e) {
            System.err.println("Error deleting event: " + e.getMessage());
        }

        return false;
    }

    // Helper method to map ResultSet to Event object
    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
        Event event = new Event();

        event.setEventId(rs.getInt("event_id"));
        event.setEventName(rs.getString("event_name"));
        event.setDescription(rs.getString("description"));
        event.setEventType(rs.getString("event_type"));
        event.setLocation(rs.getString("location"));
        event.setEventDate(rs.getDate("event_date").toLocalDate());
        event.setStartTime(rs.getTime("start_time").toLocalTime());
        event.setEndTime(rs.getTime("end_time").toLocalTime());
        event.setMaxParticipants(rs.getInt("max_participants"));
        event.setCurrentParticipants(rs.getInt("current_participants"));
        event.setOrganizerUsername(rs.getString("organizer_username"));
        event.setOrganizerName(rs.getString("organizer_name"));
        event.setFaculty(rs.getString("faculty"));
        event.setRequirements(rs.getString("requirements"));
        event.setContactInfo(rs.getString("contact_info"));
        event.setRegistrationOpen(rs.getBoolean("registration_open"));
        event.setStatus(rs.getString("status"));
        event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        event.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return event;
    }

    // Get upcoming events (useful for dashboard)
    public List<Event> getUpcomingEvents(int limit) {
        List<Event> events = new ArrayList<>();
        String sql = """
            SELECT e.*, COUNT(ep.username) as current_participants
            FROM events e
            LEFT JOIN event_participants ep ON e.event_id = ep.event_id
            WHERE e.status = 'UPCOMING' AND e.event_date >= CURDATE()
            GROUP BY e.event_id
            ORDER BY e.event_date ASC, e.start_time ASC
            LIMIT ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);
                events.add(event);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching upcoming events: " + e.getMessage());
        }

        return events;
    }
}