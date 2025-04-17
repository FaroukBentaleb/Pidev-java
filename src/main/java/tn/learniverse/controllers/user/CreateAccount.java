package tn.learniverse.controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.learniverse.entities.*;
import tn.learniverse.services.*;
import tn.learniverse.tools.Navigator;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import org.mindrot.jbcrypt.BCrypt;

public class CreateAccount implements Initializable {

    private final UserService userService = new UserService();
    public Button login_btn;
    public ImageView logoImageView;

    @FXML
    private TextField signup_email;

    @FXML
    private TextField signup_family_name;

    @FXML
    private TextField signup_name;

    @FXML
    private ChoiceBox<String> signup_role;

    @FXML
    private PasswordField  signup_conf_pwd;

    @FXML
    private PasswordField  signup_pwd;

    @FXML
    private Label error_name;

    @FXML
    private Label error_family_name;

    @FXML
    private Label error_email;

    @FXML
    private Label error_pwd;

    @FXML
    private Label error_conf_pwd;

    @FXML
    void Signup_btn(ActionEvent event) {
        if (signup_pwd.getText().equals(signup_conf_pwd.getText())){
            boolean isValid = true;
            String name = signup_name.getText().trim();
            String familyName = signup_family_name.getText().trim();
            String email = signup_email.getText().trim();
            String role = signup_role.getValue();
            String pwd = signup_pwd.getText();
            String confPwd = signup_conf_pwd.getText();
            if (name.isEmpty() || familyName.isEmpty() || email.isEmpty() || role == null || pwd.isEmpty() || confPwd.isEmpty()) {
                Navigator.showAlert(Alert.AlertType.WARNING, "Champs vides", "Veuillez remplir tous les champs.");
                return;
            }

            // Proceed only if all valid
            if (isValid) {
                try {
                    String hashedPassword = BCrypt.hashpw(pwd, BCrypt.gensalt());
                    System.out.println("pwd: " + pwd);
                    System.out.println("hashedPassword: " + hashedPassword);
                    userService.CreateAccount(new User(name,familyName,email,role,hashedPassword));
                    Navigator.showAlert(Alert.AlertType.INFORMATION, "Account Created Successfully", "You can proceed to logging in");
                    Navigator.redirect(event, "/fxml/user/Login.fxml");
                }
                catch (SQLException e){
                    Navigator.showAlert(Alert.AlertType.ERROR, "Error creating account", "Something went wrong");
                }
            }
        }
        else{
            Navigator.showAlert(Alert.AlertType.ERROR, "Password Mismatch", "The passwords you entered do not match. Please try again.");

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logoImageView.setImage(new Image("file:/C:/wamp64/www/images/logo/logo.png"));
        signup_role.getItems().addAll("Student", "Insctructor");
        signup_role.setValue("Student");
        setupRealTimeValidation();
    }

    public void Login_btn(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent, "/fxml/user/Login.fxml");
    }
    private void setupRealTimeValidation() {
        // Name
        signup_name.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().length() < 2) {
                signup_name.setStyle("-fx-border-color: red;");
                error_name.setText("Name must be at least 2 characters");
            } else {
                signup_name.setStyle(null);
                error_name.setText("");
            }
        });

        // Family Name
        signup_family_name.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().length() < 2) {
                signup_family_name.setStyle("-fx-border-color: red;");
                error_family_name.setText("Family name must be at least 2 characters");
            } else {
                signup_family_name.setStyle(null);
                error_family_name.setText("");
            }
        });

        // Email
        signup_email.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                signup_email.setStyle("-fx-border-color: red;");
                error_email.setText("Invalid email format");
            } else {
                signup_email.setStyle(null);
                error_email.setText("");
            }
        });

        // Password
        signup_pwd.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")) {
                signup_pwd.setStyle("-fx-border-color: red;");
                error_pwd.setText("8+ chars, upper & lower case, special char");
            } else {
                signup_pwd.setStyle(null);
                error_pwd.setText("");
            }
        });

        // Confirm Password
        signup_conf_pwd.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(signup_pwd.getText())) {
                signup_conf_pwd.setStyle("-fx-border-color: red;");
                error_conf_pwd.setText("Passwords do not match");
            } else {
                signup_conf_pwd.setStyle(null);
                error_conf_pwd.setText("");
            }
        });

        // Sync confirm password check when password field changes
        signup_pwd.textProperty().addListener((obs, oldVal, newVal) -> {
            String conf = signup_conf_pwd.getText();
            if (!conf.equals(newVal)) {
                signup_conf_pwd.setStyle("-fx-border-color: red;");
                error_conf_pwd.setText("Passwords do not match");
            } else {
                signup_conf_pwd.setStyle(null);
                error_conf_pwd.setText("");
            }
        });
    }

}
