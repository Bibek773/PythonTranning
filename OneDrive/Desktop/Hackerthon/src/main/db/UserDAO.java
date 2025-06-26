package main.db;

import main.model.User;
import java.sql.*;

public class UserDAO {

    /**
     * Register new user in database
     * @param user User object with registration data
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(User user) {
        // Updated SQL to match the User model field mappings
        String sql = "INSERT INTO users (email, username, full_name, phone_number, user_type, roll_no, semester, faculty, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("Database connection failed!");
                return false;
            }

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getPhone()); // Using getPhone() instead of getPhoneNumber()
            stmt.setString(5, user.getUserType());

            // Handle nullable fields for teachers
            if (user.getStudentId() != null && !user.getStudentId().trim().isEmpty()) {
                stmt.setString(6, user.getStudentId()); // Using getStudentId() instead of getRollNo()
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            if (user.getYearLevel() != null) {
                stmt.setInt(7, user.getYearLevel()); // Using getYearLevel() instead of getSemester()
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.setString(8, user.getCourse()); // Using getCourse() instead of getFaculty()
            stmt.setString(9, user.getPassword());

            int rowsAffected = stmt.executeUpdate();
            System.out.println("User registration: " + rowsAffected + " row(s) affected");

            return rowsAffected > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("Email or username already exists: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Authenticate user login
     * @param emailOrUsername User email or username
     * @param password User password
     * @return User object if login successful, null otherwise
     */
    public User loginUser(String emailOrUsername, String password) {
        // Updated SQL to allow login with either email or username
        String sql = "SELECT * FROM users WHERE (email = ? OR username = ?) AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("Database connection failed!");
                return null;
            }

            stmt.setString(1, emailOrUsername);
            stmt.setString(2, emailOrUsername);
            stmt.setString(3, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Create User object using the new field mappings
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setPhone(rs.getString("phone_number")); // Map to new field name
                user.setUserType(rs.getString("user_type"));

                // Handle nullable fields
                String rollNo = rs.getString("roll_no");
                if (!rs.wasNull()) {
                    user.setStudentId(rollNo); // Map to new field name
                }

                int semester = rs.getInt("semester");
                if (!rs.wasNull()) {
                    user.setYearLevel(semester); // Map to new field name
                }

                user.setCourse(rs.getString("faculty")); // Map to new field name
                user.setPassword(rs.getString("password"));

                System.out.println("Login successful for: " + user.getEmail());
                return user;
            } else {
                System.out.println("Invalid credentials for: " + emailOrUsername);
                return null;
            }

        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if email already exists in database
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("Database connection failed!");
                return false;
            }

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Email check for '" + email + "': " + (count > 0 ? "exists" : "available"));
                return count > 0;
            }

        } catch (SQLException e) {
            System.err.println("Database error checking email: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Check if username already exists in database
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("Database connection failed!");
                return false;
            }

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Username check for '" + username + "': " + (count > 0 ? "exists" : "available"));
                return count > 0;
            }

        } catch (SQLException e) {
            System.err.println("Database error checking username: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get user by ID
     * @param id User ID
     * @return User object if found, null otherwise
     */
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("Database connection failed!");
                return null;
            }

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setPhone(rs.getString("phone_number")); // Map to new field name
                user.setUserType(rs.getString("user_type"));

                // Handle nullable fields
                String rollNo = rs.getString("roll_no");
                if (!rs.wasNull()) {
                    user.setStudentId(rollNo); // Map to new field name
                }

                int semester = rs.getInt("semester");
                if (!rs.wasNull()) {
                    user.setYearLevel(semester); // Map to new field name
                }

                user.setCourse(rs.getString("faculty")); // Map to new field name
                user.setPassword(rs.getString("password"));

                return user;
            }

        } catch (SQLException e) {
            System.err.println("Database error getting user by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Update user information
     * @param user User object with updated information
     * @return true if update successful, false otherwise
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET email = ?, username = ?, full_name = ?, phone_number = ?, user_type = ?, roll_no = ?, semester = ?, faculty = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("Database connection failed!");
                return false;
            }

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getPhone()); // Using getPhone()
            stmt.setString(5, user.getUserType());

            // Handle nullable fields
            if (user.getStudentId() != null && !user.getStudentId().trim().isEmpty()) {
                stmt.setString(6, user.getStudentId()); // Using getStudentId()
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            if (user.getYearLevel() != null) {
                stmt.setInt(7, user.getYearLevel()); // Using getYearLevel()
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.setString(8, user.getCourse()); // Using getCourse()
            stmt.setInt(9, user.getId());

            int rowsAffected = stmt.executeUpdate();
            System.out.println("User update: " + rowsAffected + " row(s) affected");

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Database error during user update: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}