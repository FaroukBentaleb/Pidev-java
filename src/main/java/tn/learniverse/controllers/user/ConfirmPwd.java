package tn.learniverse.controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.mindrot.jbcrypt.BCrypt;
import tn.learniverse.entities.User;
import tn.learniverse.services.UserService;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.net.URL;
import java.util.ResourceBundle;

public class ConfirmPwd implements Initializable {
    public CheckBox showPasswordCheckBox;
    public Label error_pwd;
    public TextField textField;
    public Label error_confpwd;
    public PasswordField confpwd;
    public PasswordField pwd;
    public Label email;
    public TextField textFieldconf;
    public ImageView logoImageView;
    @FXML
    public ChoiceBox<String> role;

    public void Next_btn(ActionEvent actionEvent) {
        if(validatePWD()){
            try {
                UserService userService = new UserService();
                String hashedPassword = BCrypt.hashpw(pwd.getText(), BCrypt.gensalt());
                hashedPassword = hashedPassword.replace("$2a$", "$2y$");
                Session.getCurrentUser().setRole(role.getValue());
                userService.CreateAccount(new User(Session.getCurrentUser().getNom(),Session.getCurrentUser().getPrenom(),Session.getCurrentUser().getEmail(),role.getValue(),hashedPassword));
                Navigator.redirect(actionEvent, "/fxml/homePage.fxml");
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        role.getItems().addAll("Student", "Insctructor");
        role.setValue("Student");
        this.email.setText(Session.getCurrentUser().getEmail());
        setupRealTimeValidation();
        logoImageView.setImage(new Image("file:///C:/wamp64/www/images/logo/logo.png"));
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
