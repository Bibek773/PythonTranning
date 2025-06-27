// ConfigurationManager.java - New utility class for managing configs
package main.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationManager {

    private static Properties appProperties;

    static {
        loadConfiguration();
    }

    /**
     * Load application configuration
     */
    private static void loadConfiguration() {
        appProperties = new Properties();

        try (InputStream input = ConfigurationManager.class.getClassLoader()
                .getResourceAsStream("config/app.properties")) {

            if (input != null) {
                appProperties.load(input);
                System.out.println("Application configuration loaded successfully");
            } else {
                System.err.println("app.properties file not found, using defaults");
            }

        } catch (IOException e) {
            System.err.println("Error loading application configuration: " + e.getMessage());
        }
    }

    /**
     * Get property value
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value
     */
    public static String getProperty(String key, String defaultValue) {
        return appProperties.getProperty(key, defaultValue);
    }

    /**
     * Get property value
     * @param key Property key
     * @return Property value or null if not found
     */
    public static String getProperty(String key) {
        return appProperties.getProperty(key);
    }

    /**
     * Check if running in development mode
     * @return true if development mode
     */
    public static boolean isDevelopmentMode() {
        return "development".equalsIgnoreCase(getProperty("app.environment", "production"));
    }

    /**
     * Get application version
     * @return Application version
     */
    public static String getAppVersion() {
        return getProperty("app.version", "1.0.0");
    }
}