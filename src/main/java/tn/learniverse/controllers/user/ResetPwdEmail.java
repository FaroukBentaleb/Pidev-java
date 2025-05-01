package tn.learniverse.controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.learniverse.entities.User;
import tn.learniverse.services.PasswordResetService;
import tn.learniverse.services.UserService;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import javax.mail.*;
import javax.mail.internet.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class ResetPwdEmail implements Initializable {
    @FXML
    public ImageView logoImageView;
    @FXML
    public TextField login_email;
    @FXML
    public Label error_email;

    private UserService userService = new UserService();

    @FXML
    public void Next_btn(ActionEvent actionEvent) {
        if (login_email.getText().isEmpty()) {
            login_email.setStyle("-fx-border-color: red;");
            showError("Email required to proceed!");
            return;
        }

        if (!login_email.getText().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            login_email.setStyle("-fx-border-color: red;");
            showError("Invalid email format");
            return;
        }

        // Check if email exists in the system
        User user = userService.getUserByEmail(login_email.getText());
        if (user == null || user.getEmail() == null) {
            showError("Given email does not match any account, please check your credentials!");
            return;
        }

        // Generate a reset token
        String resetToken = PasswordResetService.createResetToken(user.getEmail());

        // Store email in session
        Session.setEmail(user.getEmail());

        // Send reset email
        boolean emailSent = sendPasswordResetEmail(user.getEmail(), resetToken, user.getNom());

        if (emailSent) {
            // Show success message
            error_email.setVisible(true);
            error_email.setStyle("-fx-text-fill: green;");
            error_email.setText("Password reset link sent to your email!");

            // Redirect to login page after a short delay
            // You can use a JavaFX Timeline for this if you want a delay
            // For now, just redirect back to login
            Navigator.showAlert(Alert.AlertType.INFORMATION,"Reset Email Sent","Please check your inbox!");
        } else {
            showError("Failed to send reset email. Please try again later.");
        }
    }

    @FXML
    public void Back_btn(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent, "/fxml/user/Login.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logoImageView.setImage(new Image("file:///C:/wamp64/www/images/logo/logo(white).png"));
        setupRealTimeValidation();
        error_email.setVisible(false);
    }

    private void setupRealTimeValidation() {
        login_email.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                login_email.setStyle("-fx-border-color: red;");
                error_email.setVisible(true);
                error_email.setStyle("-fx-text-fill: red;");
                error_email.setText("Invalid email format");
            } else {
                login_email.setStyle(null);
                error_email.setVisible(false);
                error_email.setText("");
            }
        });
    }

    private void showError(String message) {
        error_email.setVisible(true);
        error_email.setStyle("-fx-text-fill: red;");
        error_email.setText(message);
    }

    private boolean sendPasswordResetEmail(String recipientEmail, String resetToken, String userName) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Email account credentials
        final String senderEmail = "bytequest.pro@gmail.com";
        final String senderPassword = "wlmzixzfblxlzmsc";

        // Create session with authentication
        javax.mail.Session session = javax.mail.Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Password Reset Request - Learniverse");

            // Build reset link with token
            String resetLink = "http://localhost:8081/reset-password?token=" + resetToken;
            Session.setUrl(resetLink);

            // Create HTML content for the email
            String htmlContent =
                    "<div style='max-width: 600px; margin: 0 auto; padding: 20px; font-family: \"Segoe UI\", Arial, sans-serif; color: #2d3748; background: linear-gradient(135deg, #f6f8fc 0%, #e9effd 100%);'>" +
                            "<div style='text-align: center; padding: 20px;'>" +
                            "<h1 style='color: #4a5568; font-size: 32px; margin: 0; text-shadow: 2px 2px 4px rgba(0,0,0,0.1);'>" +
                            "<span style='color: #6200ea; font-weight: 800;'>Learni</span><span style='color: #3700b3; font-weight: 800;'>verse</span>" +
                            "</h1>" +
                            "</div>" +

                            "<div style='background-color: #ffffff; padding: 40px; border-radius: 20px; box-shadow: 0 10px 25px rgba(98,0,234,0.1); border: 1px solid #e2e8f0;'>" +
                            "<div style='text-align: center; margin-bottom: 30px;'>" +
                            "<div style='background: linear-gradient(135deg, #6200ea 0%, #3700b3 100%); width: 80px; height: 80px; border-radius: 50%; margin: 0 auto; padding: 20px; box-shadow: 0 8px 16px rgba(98,0,234,0.2);'>" +
                            "<span style='font-size: 40px; color: #ffffff;'>🔒</span>" +
                            "</div>" +
                            "</div>" +

                            "<h2 style='color: #1a202c; font-size: 28px; margin-bottom: 20px; text-align: center; font-weight: 700;'>Reset Your Password</h2>" +

                            "<p style='font-size: 16px; line-height: 1.8; margin-bottom: 25px; color: #4a5568;'>" +
                            "Hello " + userName + "," +
                            "</p>" +

                            "<p style='font-size: 16px; line-height: 1.8; margin-bottom: 25px; color: #4a5568;'>" +
                            "We received a request to reset your password. Click the button below to create a new password:" +
                            "</p>" +

                            "<div style='text-align: center; margin: 35px 0;'>" +
                            "<a href='" + resetLink + "' " +
                            "style='display: inline-block; background: linear-gradient(135deg, #6200ea 0%, #3700b3 100%); color: white; padding: 16px 40px; text-decoration: none; border-radius: 12px; font-weight: 600; font-size: 16px; transition: all 0.3s ease; box-shadow: 0 8px 15px rgba(98,0,234,0.2);'>" +
                            "Reset Password" +
                            "</a>" +
                            "</div>" +

                            "<p style='font-size: 14px; color: #718096; margin-bottom: 20px; text-align: center; padding: 20px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5fd 100%); border-radius: 12px; border: 1px solid #e2e8f0;'>" +
                            "This link will expire in 24 hours." +
                            "</p>" +

                            "<p style='font-size: 14px; color: #718096; text-align: center;'>" +
                            "<span style='color: #dc2626; display: block; margin-top: 10px;'>" +
                            "If you didn't request a password reset, please take this seriously and review your account security immediately." +
                            "</span>" +
                            "</p>" +
                            "</div>" +

                            "<div style='text-align: center; margin-top: 30px; color: #718096; font-size: 14px;'>" +
                            "<p style='font-weight: 600;'>© " + java.time.Year.now().getValue() + " Learniverse. All rights reserved.</p>" +
                            "<p style='margin-top: 10px; letter-spacing: 1px;'>⚡ Explore • Learn • Innovate ⚡</p>" +
                            "</div>" +
                            "</div>";

            // Set content type to HTML
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Send message
            Transport.send(message);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}