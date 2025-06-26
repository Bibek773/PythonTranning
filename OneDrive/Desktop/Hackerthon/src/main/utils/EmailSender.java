package main.utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailSender {

    // Gmail SMTP Configuration
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "bibekghimire773@gmail.com"; // Replace with your Gmail
    private static final String SENDER_APP_PASSWORD = "ecbdsmwwtivbksgn"; // Replace with your App Password

    /**
     * Send OTP email to user
     * @param recipientEmail Recipient's email address
     * @param otp Generated OTP code
     * @return true if email sent successfully, false otherwise
     */
    public boolean sendOTP(String recipientEmail, String otp) {
        try {
            // Create properties for Gmail SMTP
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            // Timeout settings
            props.put("mail.smtp.connectiontimeout", "30000");
            props.put("mail.smtp.timeout", "30000");
            props.put("mail.smtp.writetimeout", "30000");

            // Create authenticator
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
                }
            };

            // Create session
            Session session = Session.getInstance(props, authenticator);

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "College App Team"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("College App - Email Verification OTP");

            // Create email content
            String emailContent = createOTPEmailContent(otp);
            message.setContent(emailContent, "text/html; charset=utf-8");

            // Add headers for better deliverability
            message.addHeader("Return-Path", SENDER_EMAIL);
            message.addHeader("X-Mailer", "College App");
            message.addHeader("X-Priority", "1");

            // Send email
            Transport.send(message);

            System.out.println("✅ OTP email sent successfully to: " + recipientEmail);
            System.out.println("📧 Email will be delivered to the recipient's inbox");
            System.out.println("⚠️  If not in inbox, check SPAM folder");
            return true;

        } catch (MessagingException e) {
            System.err.println("❌ Error sending OTP email: " + e.getMessage());

            // Specific error messages
            if (e.getMessage().contains("Authentication failed")) {
                System.err.println("🔐 AUTHENTICATION ERROR:");
                System.err.println("   1. Make sure 2FA is enabled on your Gmail");
                System.err.println("   2. Generate an App Password (16 digits)");
                System.err.println("   3. Use the App Password, not your regular password");
            } else if (e.getMessage().contains("Could not connect")) {
                System.err.println("🌐 CONNECTION ERROR:");
                System.err.println("   1. Check your internet connection");
                System.err.println("   2. Verify SMTP settings");
            }

            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send welcome email to new user
     * @param recipientEmail Recipient's email address
     * @param userName User's full name
     * @return true if email sent successfully, false otherwise
     */
    public boolean sendWelcomeEmail(String recipientEmail, String userName) {
        try {
            Properties props = createGmailProperties();

            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
                }
            };

            Session session = Session.getInstance(props, authenticator);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "College App Team"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Welcome to College App!");

            String emailContent = createWelcomeEmailContent(userName);
            message.setContent(emailContent, "text/html; charset=utf-8");

            // Headers for better deliverability
            message.addHeader("Return-Path", SENDER_EMAIL);
            message.addHeader("X-Mailer", "College App");

            Transport.send(message);

            System.out.println("✅ Welcome email sent successfully to: " + recipientEmail);
            System.out.println("📧 Email will be delivered to the recipient's inbox");
            return true;

        } catch (MessagingException e) {
            System.err.println("❌ Error sending welcome email: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create Gmail SMTP properties
     */
    private Properties createGmailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.smtp.connectiontimeout", "30000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.writetimeout", "30000");
        return props;
    }

    /**
     * Send test email to your own email address
     */
    public boolean sendTestEmailToSelf() {
        System.out.println("📤 Sending test email to your own Gmail address...");
        return sendOTP(SENDER_EMAIL, "TEST123");
    }

    /**
     * Send test email to any email address
     */
    public boolean sendTestEmail(String recipientEmail) {
        System.out.println("📤 Sending test email to: " + recipientEmail);
        return sendOTP(recipientEmail, "TEST456");
    }

    /**
     * Create HTML content for OTP email
     */
    private String createOTPEmailContent(String otp) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px 20px; text-align: center; }" +
                ".content { padding: 40px 30px; }" +
                ".otp-container { background-color: #f8f9fa; padding: 30px; margin: 30px 0; text-align: center; border-radius: 12px; border: 2px solid #e9ecef; }" +
                ".otp-label { font-size: 16px; color: #6c757d; margin-bottom: 10px; }" +
                ".otp-code { font-size: 36px; font-weight: bold; color: #28a745; letter-spacing: 8px; font-family: 'Courier New', monospace; margin: 15px 0; }" +
                ".warning { background-color: #fff3cd; padding: 20px; margin: 20px 0; border-radius: 8px; border-left: 4px solid #ffc107; }" +
                ".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; }" +
                ".btn { display: inline-block; padding: 12px 24px; background-color: #007bff; color: white; text-decoration: none; border-radius: 6px; margin: 10px 0; }" +
                "@media (max-width: 600px) { .content { padding: 20px; } .otp-code { font-size: 28px; letter-spacing: 4px; } }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<h1 style='margin: 0; font-size: 28px;'>🎓 College App</h1>" +
                "<p style='margin: 10px 0 0 0; opacity: 0.9;'>Email Verification Required</p>" +
                "</div>" +

                "<div class=\"content\">" +
                "<h2 style='color: #343a40; margin-top: 0;'>Verify Your Email Address</h2>" +
                "<p>Hello there! 👋</p>" +
                "<p>Welcome to College App! To complete your registration and secure your account, please verify your email address using the code below:</p>" +

                "<div class=\"otp-container\">" +
                "<div class=\"otp-label\">Your Verification Code</div>" +
                "<div class=\"otp-code\">" + otp + "</div>" +
                "<p style='margin: 0; color: #6c757d; font-size: 14px;'>Enter this code in the app to continue</p>" +
                "</div>" +

                "<div class=\"warning\">" +
                "<strong>⚠️ Important Security Information:</strong>" +
                "<ul style='margin: 10px 0; padding-left: 20px;'>" +
                "<li>This code expires in <strong>10 minutes</strong></li>" +
                "<li>Never share this code with anyone</li>" +
                "<li>If you didn't request this, please ignore this email</li>" +
                "<li>Contact support if you need assistance</li>" +
                "</ul>" +
                "</div>" +

                "<p>Once verified, you'll have access to all College App features including:</p>" +
                "<ul>" +
                "<li>📚 Course materials and assignments</li>" +
                "<li>📅 Campus events and activities</li>" +
                "<li>💬 Connect with classmates</li>" +
                "<li>🎯 Academic progress tracking</li>" +
                "</ul>" +

                "<p>Thank you for joining our college community!</p>" +
                "<p style='margin-bottom: 0;'>Best regards,<br><strong>The College App Team</strong></p>" +
                "</div>" +

                "<div class=\"footer\">" +
                "<p style='margin: 0;'>This is an automated message. Please do not reply to this email.</p>" +
                "<p style='margin: 5px 0 0 0;'>© 2025 College App. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Create HTML content for welcome email
     */
    private String createWelcomeEmailContent(String userName) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; }" +
                ".header { background: linear-gradient(135deg, #28a745 0%, #20c997 100%); color: white; padding: 30px 20px; text-align: center; }" +
                ".content { padding: 40px 30px; }" +
                ".welcome-box { background: linear-gradient(135deg, #e8f5e8 0%, #f0f9ff 100%); padding: 30px; margin: 20px 0; text-align: center; border-radius: 12px; border: 2px solid #28a745; }" +
                ".features { background-color: #f8f9fa; padding: 25px; margin: 25px 0; border-radius: 8px; border-left: 4px solid #007bff; }" +
                ".feature-item { margin: 15px 0; padding: 10px 0; }" +
                ".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; }" +
                ".btn { display: inline-block; padding: 12px 24px; background-color: #28a745; color: white; text-decoration: none; border-radius: 6px; margin: 15px 0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<h1 style='margin: 0; font-size: 32px;'>🎉 Welcome to College App!</h1>" +
                "<p style='margin: 10px 0 0 0; opacity: 0.9; font-size: 18px;'>Your Gateway to Campus Life</p>" +
                "</div>" +

                "<div class=\"content\">" +
                "<div class=\"welcome-box\">" +
                "<h2 style='color: #28a745; margin-top: 0;'>Welcome, " + userName + "! 🎓</h2>" +
                "<p style='font-size: 18px; margin-bottom: 0;'>Your account has been successfully created and verified!</p>" +
                "</div>" +

                "<p>We're thrilled to have you join our vibrant college community platform. College App is designed to enhance your campus experience and keep you connected with everything that matters.</p>" +

                "<div class=\"features\">" +
                "<h3 style='color: #007bff; margin-top: 0;'>🚀 What's waiting for you:</h3>" +

                "<div class=\"feature-item\">" +
                "<strong>📚 Academic Hub:</strong> Access course materials, assignments, and track your academic progress" +
                "</div>" +

                "<div class=\"feature-item\">" +
                "<strong>📅 Campus Events:</strong> Stay updated with the latest events, activities, and important announcements" +
                "</div>" +

                "<div class=\"feature-item\">" +
                "<strong>💬 Connect & Chat:</strong> Network with classmates, join study groups, and collaborate on projects" +
                "</div>" +

                "<div class=\"feature-item\">" +
                "<strong>🎯 Personalized Experience:</strong> Customize your feed and get recommendations based on your interests" +
                "</div>" +

                "<div class=\"feature-item\">" +
                "<strong>🏆 Achievements:</strong> Track your milestones and celebrate your academic journey" +
                "</div>" +
                "</div>" +

                "<div style='text-align: center; margin: 30px 0;'>" +
                "<p style='font-size: 16px;'>Ready to get started?</p>" +
                "<a href='#' class=\"btn\">🚀 Launch College App</a>" +
                "</div>" +

                "<p>If you have any questions or need assistance, our support team is here to help. Don't hesitate to reach out!</p>" +

                "<p>Here's to an amazing academic journey ahead! 🌟</p>" +

                "<p style='margin-bottom: 0;'>Warmest welcome,<br><strong>The College App Team</strong></p>" +
                "</div>" +

                "<div class=\"footer\">" +
                "<p style='margin: 0;'>This is an automated welcome message. Please do not reply to this email.</p>" +
                "<p style='margin: 5px 0 0 0;'>© 2025 College App. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Test Gmail configuration
     */
    public boolean testEmailConfiguration() {
        System.out.println("🔧 Testing Gmail configuration...");
        System.out.println("📧 Email: " + SENDER_EMAIL);
        System.out.println("🌐 SMTP Host: " + SMTP_HOST);
        System.out.println("🔌 Port: " + SMTP_PORT);

        try {
            Properties props = createGmailProperties();

            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
                }
            };

            Session session = Session.getInstance(props, authenticator);
            Transport transport = session.getTransport("smtp");

            System.out.println("🔄 Attempting to connect to Gmail...");
            transport.connect(SMTP_HOST, SENDER_EMAIL, SENDER_APP_PASSWORD);

            System.out.println("✅ Successfully connected to Gmail!");
            transport.close();

            System.out.println("✅ Gmail configuration test passed!");
            return true;

        } catch (MessagingException e) {
            System.err.println("❌ Gmail configuration test failed: " + e.getMessage());

            if (e.getMessage().contains("Authentication failed")) {
                System.err.println("\n🔐 GMAIL SETUP REQUIRED:");
                System.err.println("1. Go to https://myaccount.google.com/security");
                System.err.println("2. Enable 2-Step Verification");
                System.err.println("3. Generate App Password:");
                System.err.println("   - Go to App passwords");
                System.err.println("   - Select 'Mail' and 'Other (custom name)'");
                System.err.println("   - Copy the 16-digit password");
                System.err.println("4. Update SENDER_APP_PASSWORD in your code");
            }

            return false;
        }
    }
}