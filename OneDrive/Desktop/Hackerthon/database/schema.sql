-- College App Database Schema
-- Create database if not exists
CREATE DATABASE IF NOT EXISTS collegeapp;
USE collegeapp;

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS assignments;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS users;

-- Users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    student_id VARCHAR(20),
    course VARCHAR(100),
    year_level INT,
    profile_picture VARCHAR(255),
    is_verified BOOLEAN DEFAULT FALSE,
    is_online BOOLEAN DEFAULT FALSE,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_online (is_online)
);

-- Events table
CREATE TABLE events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    event_date DATE NOT NULL,
    event_time TIME,
    location VARCHAR(255),
    organizer_username VARCHAR(50) NOT NULL,
    max_participants INT DEFAULT 0,
    current_participants INT DEFAULT 0,
    category VARCHAR(50) DEFAULT 'General',
    status ENUM('Active', 'Cancelled', 'Completed') DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organizer_username) REFERENCES users(username) ON DELETE CASCADE,
    INDEX idx_event_date (event_date),
    INDEX idx_organizer (organizer_username),
    INDEX idx_status (status)
);

-- Event participants table (for tracking who joined events)
CREATE TABLE event_participants (
    id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    UNIQUE KEY unique_participant (event_id, username)
);

-- Announcements table
CREATE TABLE announcements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author_username VARCHAR(50) NOT NULL,
    priority ENUM('Low', 'Medium', 'High', 'Urgent') DEFAULT 'Medium',
    category VARCHAR(50) DEFAULT 'General',
    is_pinned BOOLEAN DEFAULT FALSE,
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_username) REFERENCES users(username) ON DELETE CASCADE,
    INDEX idx_priority (priority),
    INDEX idx_created_at (created_at),
    INDEX idx_pinned (is_pinned)
);

-- Assignments table
CREATE TABLE assignments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    subject VARCHAR(100) NOT NULL,
    instructor_username VARCHAR(50) NOT NULL,
    due_date DATETIME NOT NULL,
    total_marks INT DEFAULT 100,
    assignment_type ENUM('Quiz', 'Project', 'Essay', 'Lab', 'Exam') DEFAULT 'Project',
    status ENUM('Draft', 'Published', 'Completed') DEFAULT 'Draft',
    file_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (instructor_username) REFERENCES users(username) ON DELETE CASCADE,
    INDEX idx_due_date (due_date),
    INDEX idx_subject (subject),
    INDEX idx_instructor (instructor_username)
);

-- Assignment submissions table
CREATE TABLE assignment_submissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    assignment_id INT NOT NULL,
    student_username VARCHAR(50) NOT NULL,
    submission_text TEXT,
    file_path VARCHAR(255),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    marks_obtained INT DEFAULT NULL,
    feedback TEXT,
    status ENUM('Submitted', 'Graded', 'Late') DEFAULT 'Submitted',
    FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    FOREIGN KEY (student_username) REFERENCES users(username) ON DELETE CASCADE,
    UNIQUE KEY unique_submission (assignment_id, student_username)
);

-- Messages table for chat functionality
CREATE TABLE messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_username VARCHAR(50) NOT NULL,
    receiver_username VARCHAR(50) NULL, -- NULL for group messages
    chat_type VARCHAR(50) NOT NULL DEFAULT 'General',
    message_text TEXT NOT NULL,
    message_type ENUM('text', 'file', 'image') DEFAULT 'text',
    file_path VARCHAR(255) NULL,
    is_edited BOOLEAN DEFAULT FALSE,
    edited_at TIMESTAMP NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_username) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (receiver_username) REFERENCES users(username) ON DELETE CASCADE,
    INDEX idx_chat_type (chat_type),
    INDEX idx_timestamp (timestamp),
    INDEX idx_sender (sender_username)
);

-- OTP verification table for email verification
CREATE TABLE otp_verification (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    purpose ENUM('signup', 'password_reset') NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    INDEX idx_email (email),
    INDEX idx_otp (otp_code)
);

-- Notification table for system notifications
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('info', 'warning', 'success', 'error') DEFAULT 'info',
    is_read BOOLEAN DEFAULT FALSE,
    related_id INT NULL, -- Can reference events, assignments, etc.
    related_type VARCHAR(50) NULL, -- 'event', 'assignment', 'announcement'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    INDEX idx_username (username),
    INDEX idx_read_status (is_read),
    INDEX idx_created_at (created_at)
);

-- User roles table (for future admin/teacher roles)
CREATE TABLE user_roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    role ENUM('student', 'teacher', 'admin') DEFAULT 'student',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(50),
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(username) ON DELETE SET NULL,
    UNIQUE KEY unique_user_role (username, role)
);

-- Insert default admin user (password: admin123 - remember to hash in production)
INSERT INTO users (username, email, password, full_name, course, year_level, is_verified) VALUES
('admin', 'admin@collegeapp.com', 'admin123', 'System Administrator', 'Computer Science', 4, TRUE);

-- Insert admin role
INSERT INTO user_roles (username, role) VALUES ('admin', 'admin');

-- Insert sample data for testing

-- Sample users
INSERT INTO users (username, email, password, full_name, phone, student_id, course, year_level, is_verified) VALUES
('john_doe', 'john@example.com', 'password123', 'John Doe', '+977-9800000001', 'CS001', 'Computer Science', 2, TRUE),
('jane_smith', 'jane@example.com', 'password123', 'Jane Smith', '+977-9800000002', 'CS002', 'Computer Science', 3, TRUE),
('mike_wilson', 'mike@example.com', 'password123', 'Mike Wilson', '+977-9800000003', 'EE001', 'Electrical Engineering', 1, TRUE);

-- Sample events
INSERT INTO events (title, description, event_date, event_time, location, organizer_username, max_participants, category) VALUES
('Programming Competition', 'Annual coding competition for all students', '2025-07-15', '10:00:00', 'Computer Lab A', 'admin', 50, 'Academic'),
('Tech Seminar', 'Latest trends in technology and software development', '2025-07-20', '14:00:00', 'Auditorium', 'admin', 100, 'Academic'),
('Sports Day', 'Annual college sports competition', '2025-08-01', '09:00:00', 'Sports Ground', 'admin', 200, 'Sports');

-- Sample announcements
INSERT INTO announcements (title, content, author_username, priority, category, is_pinned) VALUES
('Welcome to College App!', 'Welcome to our new college management system. Please update your profile information.', 'admin', 'High', 'System', TRUE),
('Library Hours Update', 'Library will be open extended hours during exam week from 8 AM to 10 PM.', 'admin', 'Medium', 'Academic', FALSE),
('Cafeteria Menu Change', 'New healthy options have been added to the cafeteria menu starting next week.', 'admin', 'Low', 'General', FALSE);

-- Sample assignments
INSERT INTO assignments (title, description, subject, instructor_username, due_date, total_marks, assignment_type) VALUES
('Data Structures Project', 'Implement a binary search tree with all basic operations', 'Data Structures', 'admin', '2025-07-30 23:59:59', 100, 'Project'),
('Database Design Quiz', 'Quiz on normalization and ER diagrams', 'Database Systems', 'admin', '2025-07-25 15:00:00', 50, 'Quiz'),
('Web Development Assignment', 'Create a responsive website using HTML, CSS, and JavaScript', 'Web Development', 'admin', '2025-08-05 23:59:59', 150, 'Project');

-- Sample messages
INSERT INTO messages (sender_username, chat_type, message_text) VALUES
('admin', 'General', 'Welcome everyone to the College App chat system!'),
('john_doe', 'General', 'Thanks! This looks great.'),
('jane_smith', 'Study Group', 'Anyone wants to form a study group for the upcoming exams?'),
('mike_wilson', 'General', 'When is the next programming competition?');

-- Sample notifications
INSERT INTO notifications (username, title, message, type, related_type) VALUES
('john_doe', 'Welcome!', 'Welcome to College App! Please complete your profile.', 'info', 'system'),
('jane_smith', 'New Assignment', 'A new assignment has been posted in Data Structures.', 'info', 'assignment'),
('mike_wilson', 'Event Reminder', 'Programming Competition is coming up in 2 weeks!', 'warning', 'event');

-- Create indexes for better performance
CREATE INDEX idx_messages_chat_timestamp ON messages(chat_type, timestamp);
CREATE INDEX idx_events_date_status ON events(event_date, status);
CREATE INDEX idx_assignments_due_date ON assignments(due_date, status);
CREATE INDEX idx_notifications_user_read ON notifications(username, is_read);

-- View for getting user stats
CREATE VIEW user_stats AS
SELECT
    u.username,
    u.full_name,
    COUNT(DISTINCT e.id) as events_organized,
    COUNT(DISTINCT ep.event_id) as events_joined,
    COUNT(DISTINCT a.id) as assignments_created,
    COUNT(DISTINCT asub.assignment_id) as assignments_submitted,
    COUNT(DISTINCT m.id) as messages_sent
FROM users u
LEFT JOIN events e ON u.username = e.organizer_username
LEFT JOIN event_participants ep ON u.username = ep.username
LEFT JOIN assignments a ON u.username = a.instructor_username
LEFT JOIN assignment_submissions asub ON u.username = asub.student_username
LEFT JOIN messages m ON u.username = m.sender_username
GROUP BY u.username, u.full_name;