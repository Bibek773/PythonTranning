package main.java.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.controller.LoginController;
import main.controller.SignupController;
import main.controller.DashboardController;
import main.model.User;

public class LoginManager {
    private Stage primaryStage;
    private Scene loginScene;
    private Scene signupScene;
    private Scene dashboardScene;

    public LoginManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Show login screen
     */
    public void showLoginScreen() {
        try {
            if (loginScene == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();

                LoginController controller = loader.getController();
                controller.setLoginManager(this);

                loginScene = new Scene(root, 400, 500);
            }

            primaryStage.setTitle("College App - Login");
            primaryStage.setScene(loginScene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading login screen: " + e.getMessage());
        }
    }

    /**
     * Show signup screen
     */
    public void showSignupScreen() {
        try {
            if (signupScene == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signup.fxml"));
                Parent root = loader.load();

                SignupController controller = loader.getController();
                controller.setLoginManager(this);

                signupScene = new Scene(root, 450, 650);
            }

            primaryStage.setTitle("College App - Sign Up");
            primaryStage.setScene(signupScene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading signup screen: " + e.getMessage());
        }
    }

    /**
     * Handle successful authentication
     * @param user Authenticated user
     */
    public void authenticated(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setUser(user);
            controller.setLoginManager(this);

            dashboardScene = new Scene(root, 800, 600);
            primaryStage.setTitle("College App - Dashboard");
            primaryStage.setScene(dashboardScene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading dashboard: " + e.getMessage());
        }
    }

    /**
     * Logout and return to login screen
     */
    public void logout() {
        // Clear dashboard scene to force reload
        dashboardScene = null;
        showLoginScreen();
    }
}