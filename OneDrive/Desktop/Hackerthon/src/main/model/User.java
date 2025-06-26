package main.model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;          // Added - needed for chat system
    private String email;
    private String password;
    private String fullName;
    private String phone;             // Renamed from phoneNumber to match DB
    private String studentId;         // Renamed from rollNo to match DB
    private String course;            // Renamed from faculty to match DB
    private Integer yearLevel;        // Renamed from semester to match DB
    private String profilePicture;    // Added - for future profile pictures
    private boolean isVerified;       // Added - email verification status
    private boolean isOnline;         // Added - for chat system
    private Timestamp lastSeen;       // Added - for chat system
    private Timestamp createdAt;      // Added - account creation time
    private Timestamp updatedAt;      // Added - last update time

    // Legacy field for backward compatibility
    private String userType;          // Keep for existing code compatibility

    // Default constructor
    public User() {
        // Initialize with default values
        this.isVerified = false;
        this.isOnline = false;
    }

    // Constructor for registration (backward compatible) - FIXED TO MATCH SIGNUPCONTROLLER
    public User(String email, String fullName, String phone, String userType,
                String rollNo, Integer semester, String faculty, String password) {
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;              // Correctly mapped
        this.userType = userType;
        this.studentId = rollNo;         // Map rollNo to studentId
        this.yearLevel = semester;       // Map semester to yearLevel
        this.course = faculty;           // Map faculty to course
        this.password = password;

        // Generate username from email if not provided
        if (email != null && email.contains("@")) {
            this.username = email.substring(0, email.indexOf("@"));
        }

        // Set defaults
        this.isVerified = false;
        this.isOnline = false;
    }

    // New constructor with username (recommended for new code)
    public User(String username, String email, String fullName, String phone,
                String studentId, String course, Integer yearLevel, String password) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.studentId = studentId;
        this.course = course;
        this.yearLevel = yearLevel;
        this.password = password;
        this.isVerified = false;
        this.isOnline = false;
    }

    // Complete constructor
    public User(int id, String username, String email, String password, String fullName,
                String phone, String studentId, String course, Integer yearLevel,
                String profilePicture, boolean isVerified, boolean isOnline,
                Timestamp lastSeen, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.studentId = studentId;
        this.course = course;
        this.yearLevel = yearLevel;
        this.profilePicture = profilePicture;
        this.isVerified = isVerified;
        this.isOnline = isOnline;
        this.lastSeen = lastSeen;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters for all fields
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public Integer getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(Integer yearLevel) {
        this.yearLevel = yearLevel;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public Timestamp getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Timestamp lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Legacy getters/setters for backward compatibility
    public String getPhoneNumber() {
        return phone;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phone = phoneNumber;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getRollNo() {
        return studentId;
    }

    public void setRollNo(String rollNo) {
        this.studentId = rollNo;
    }

    public Integer getSemester() {
        return yearLevel;
    }

    public void setSemester(Integer semester) {
        this.yearLevel = semester;
    }

    public String getFaculty() {
        return course;
    }

    public void setFaculty(String faculty) {
        this.course = faculty;
    }

    // Utility methods
    public String getDisplayName() {
        return fullName != null && !fullName.trim().isEmpty() ? fullName : username;
    }

    public String getOnlineStatus() {
        return isOnline ? "Online" : "Offline";
    }

    public boolean hasProfilePicture() {
        return profilePicture != null && !profilePicture.trim().isEmpty();
    }

    // Method to check if user is a student (for role-based features)
    public boolean isStudent() {
        return userType == null || userType.equalsIgnoreCase("student");
    }

    public boolean isTeacher() {
        return userType != null && userType.equalsIgnoreCase("teacher");
    }

    public boolean isAdmin() {
        return userType != null && userType.equalsIgnoreCase("admin");
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", studentId='" + studentId + '\'' +
                ", course='" + course + '\'' +
                ", yearLevel=" + yearLevel +
                ", isOnline=" + isOnline +
                ", isVerified=" + isVerified +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id == user.id &&
                (username != null ? username.equals(user.username) : user.username == null);
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}