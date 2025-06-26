
## 📁 JavaFX Project
```
CollegeApp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── app/
│   │   │   │   └── Main.java                # App launcher
│   │   │   ├── controller/                  # JavaFX controllers (logic)
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── SignupController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── EventController.java
│   │   │   │   ├── ChatController.java
│   │   │   │   └── ClassroomController.java
│   │   │   ├── model/                       # Data models (DTOs)
│   │   │   │   ├── User.java
│   │   │   │   ├── Event.java
│   │   │   │   └── Assignment.java
│   │   │   ├── db/                          # Database utilities
│   │   │   │   ├── DatabaseConnection.java
│   │   │   │   ├── UserDAO.java
│   │   │   │   └── EventDAO.java
│   │   │   └── utils/                       # Helper functions
│   │   │       ├── EmailSender.java         # JavaMail OTP
│   │   │       └── OTPGenerator.java
│   │   ├── resources/
│   │   │   ├── fxml/                        # FXML files (UI layout)
│   │   │   │   ├── login.fxml
│   │   │   │   ├── signup.fxml
│   │   │   │   ├── dashboard.fxml
│   │   │   │   ├── event.fxml
│   │   │   │   └── chat.fxml
│   │   │   ├── css/                         # Optional: styling
│   │   │   │   └── style.css
│   │   │   ├── images/                      # App icons, logos
│   │   │   │   └── logo.png
│   │   │   └── config.properties            # DB settings or constants
├── lib/                                     # JavaFX SDK or libraries (if not using Maven)
├── database/
│   └── schema.sql                           # All table creation SQL
├── README.md
├── .gitignore
└── pom.xml (if using Maven) OR .classpath & .project
```

---
