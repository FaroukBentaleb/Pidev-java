package tn.learniverse.controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.learniverse.entities.User;
import tn.learniverse.services.PasswordResetService;
import tn.learniverse.services.UserService;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.net.URL;
import java.util.ResourceBundle;

public class ResetPwd implements Initializable {
    @FXML
    public ImageView logoImageView;
    public PasswordField pwd;
    public TextField textField;
    public Label error_pwd;
    public PasswordField confpwd;
    public TextField textFieldconf;
    public Label error_confpwd;
    public CheckBox showPasswordCheckBox;

    private UserService userService = new UserService();
    private String token;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logoImageView.setImage(new Image("file:///C:/wamp64/www/images/logo/logo(white).png"));
        setupRealTimeValidation();
        textField.textProperty().bindBidirectional(pwd.textProperty());
        textFieldconf.textProperty().bindBidirectional(confpwd.textProperty());
        showPasswordCheckBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                textField.setVisible(true);
                textField.setManaged(true);
                pwd.setVisible(false);
                pwd.setManaged(false);
                textFieldconf.setVisible(true);
                textFieldconf.setManaged(true);
                confpwd.setVisible(false);
                confpwd.setManaged(false);
            } else {
                pwd.setVisible(true);
                pwd.setManaged(true);
                textField.setVisible(false);
                textField.setManaged(false);
                confpwd.setVisible(true);
                confpwd.setManaged(true);
                textFieldconf.setVisible(false);
                textFieldconf.setManaged(false);
            }
        });
        // TODO: In a real application, get the token from the URL
        if(!(Session.getUrl() == null)){
            token = getTokenFromURL(Session.getUrl());
        }
        else{
            token = "";
        }


        // Validate token
        String email = PasswordResetService.validateToken(token);
        if (email == null) {
            error_confpwd.setVisible(true);
            error_confpwd.setStyle("-fx-text-fill: red;");
            error_confpwd.setText("Invalid or expired password reset link. Please request a new one.");
        } else {
            // Store email in session
            Session.setEmail(email);
        }
    }

    private String getTokenFromURL(String url) {
        try {
            URL link = new URL(url);
            String query = link.getQuery(); // e.g., "token=XYZ123"
            if (query != null && query.startsWith("token=")) {
                return query.substring("token=".length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // No token found or URL is malformed
    }

    @FXML
    public void Proceed_btn(ActionEvent actionEvent) {
        if (pwd.getText().isEmpty() || confpwd.getText().isEmpty()) {
            System.out.println("Both password fields are required");
            return;
        }

        if (!pwd.getText().equals(confpwd.getText())) {
            System.out.println("Passwords do not match");
            return;
        }

        if (pwd.getText().length() < 6) {
            System.out.println("Password must be at least 6 characters long");
            return;
        }

        // Get email from session
        String email = "";
        if(!Session.getEmail().isEmpty()){
            email = Session.getEmail();
            if (email == null) {
                System.out.println("Session expired. Please restart password reset process.");
                return;
            }
        }


        // Get user by email
        User user = userService.getUserByEmail(email);
        if (user == null) {
            System.out.println("User not found. Please try again.");
            return;
        }

        // Hash the new password
        String hashedPassword = PasswordResetService.hashPassword(pwd.getText());
        try{
            boolean updated = userService.ChangePwd(user.getEmail(),hashedPassword);

            if (updated) {
                // Invalidate the token
                PasswordResetService.invalidateToken(token);

                Navigator.showAlert(Alert.AlertType.INFORMATION,"Done!","Password successfully updated");

                Navigator.redirect(actionEvent, "/fxml/user/Login.fxml");
            } else {
                System.out.println("Failed to update password. Please try again.");
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    private void setupRealTimeValidation() {
        pwd.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")) {
                pwd.setStyle("-fx-border-color: red;");
                textField.setStyle("-fx-border-color: red;");
                error_pwd.setText("8+ characters (upper & lower case, special char)");
            } else {
                pwd.setStyle(null);
                textField.setStyle(null);
                error_pwd.setText("");
            }
        });

        // Confirm Password
        confpwd.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(pwd.getText())) {
                confpwd.setStyle("-fx-border-color: red;");
                textFieldconf.setStyle("-fx-border-color: red;");
                error_confpwd.setText("Passwords do not match");
            } else {
                confpwd.setStyle(null);
                textFieldconf.setStyle(null);
                error_confpwd.setText("");
            }
        });

        // Sync confirm password check when password field changes
        pwd.textProperty().addListener((obs, oldVal, newVal) -> {
            String conf = confpwd.getText();
            if (!conf.equals(newVal)) {
                confpwd.setStyle("-fx-border-color: red;");
                error_confpwd.setText("Passwords do not match");
            } else {
                confpwd.setStyle(null);
                error_confpwd.setText("");
            }
        });
    }
    private boolean validatePWD() {
        boolean isValid = true;

        if (!pwd.getText().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")) {
            pwd.setStyle("-fx-border-color: red;");
            textField.setStyle("-fx-border-color: red;");
            error_pwd.setText("8+ characters (upper & lower case, special char)");
            isValid = false;
        }
        if (!confpwd.getText().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")) {
            confpwd.setStyle("-fx-border-color: red;");
            textFieldconf.setStyle("-fx-border-color: red;");
            error_pwd.setText("8+ characters (upper & lower case, special char)");
            isValid = false;
        }
        if (!confpwd.getText().equals(pwd.getText())){
            isValid = false;
        }

        return isValid;
    }
}