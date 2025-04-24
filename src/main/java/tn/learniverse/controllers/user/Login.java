package tn.learniverse.controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.mindrot.jbcrypt.BCrypt;
import tn.learniverse.entities.*;
import tn.learniverse.services.*;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class Login implements Initializable {
    public TextField login_email;
    public PasswordField login_pwd;
    public Label error_email;
    public Label error_pwd;
    public TextField textField;
    public CheckBox showPasswordCheckBox;
    public ImageView logoImageView;

    public void Login_btn(ActionEvent actionEvent) {
        UserService userService = new UserService();
        User usr = new User();
        String login = login_email.getText();
        String pwd = login_pwd.getText();
        if(login == null || login.isEmpty() || pwd == null || pwd.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No given data");
            alert.setContentText("Please fill all the fields");
            alert.showAndWait();
        }
        else{
            if(validateLogin()){
                try {
                    usr = userService.getUserByEmail(login_email.getText());
                    if (usr != null) {
                        String storedPassword = usr.getMdp();
                        if (storedPassword != null && BCrypt.checkpw(pwd, storedPassword)) {
                            if(usr.getBan()==1){
                                Navigator.showAlert(Alert.AlertType.ERROR,"Account Banned","Your account has been Banned by the admin.\nPlease contact support for more details!");
                            }
                            else{
                                if(usr.getLogs()==0){
                                    Navigator.showAlert(Alert.AlertType.ERROR,"Account locked","Your account has been locked due to many login attempts.\nPlease reset your password or contact support for more details!");
                                }
                                else{
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Login Successful");
                                    alert.setHeaderText("Welcome !!");
                                    alert.setContentText("Connected successfully");
                                    alert.showAndWait();
                                    Session.setCurrentUser(usr);
                                    if(Session.getCurrentUser().getRole().equals("Admin")){
                                        Navigator.redirect(actionEvent, "/fxml/Back.fxml");
                                    }
                                    else{
                                        Navigator.redirect(actionEvent, "/fxml/homePage.fxml");
                                    }

                                }
                            }
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Login Failed");
                            alert.setContentText("The given email/password doesn't match any account!");
                            alert.showAndWait();
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("No User Found");
                        alert.setContentText("The given email doesn't match any account!");
                        alert.showAndWait();
                    }
                }
                catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void Signup_btn(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent, "/fxml/user/CreateAccount.fxml");
    }

    public void forgotPassword(ActionEvent actionEvent) {

    }
    private void setupRealTimeValidation() {

        // Email
        login_email.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                login_email.setStyle("-fx-border-color: red;");
                error_email.setText("Invalid email format");
            } else {
                login_email.setStyle(null);
                error_email.setText("");
            }
        });

        // Password
        login_pwd.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")) {
                login_pwd.setStyle("-fx-border-color: red;");
                textField.setStyle("-fx-border-color: red;");
                error_pwd.setText("8+ characters (upper & lower case, special char)");
            } else {
                login_pwd.setStyle(null);
                textField.setStyle(null);
                error_pwd.setText("");
            }
        });
    }
    private boolean validateLogin() {
        boolean isValid = true;

        if (!login_email.getText().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            login_email.setStyle("-fx-border-color: red;");
            error_email.setText("Invalid email format");
            isValid = false;
        }

        if (!login_pwd.getText().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")) {
            login_pwd.setStyle("-fx-border-color: red;");
            textField.setStyle("-fx-border-color: red;");
            error_pwd.setText("8+ characters (upper & lower case, special char)");
            isValid = false;
        }

        return isValid;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupRealTimeValidation();
        logoImageView.setImage(new Image("file:///C:/wamp64/www/images/logo/logo.png"));
        textField.textProperty().bindBidirectional(login_pwd.textProperty());

        // Toggle visibility
        showPasswordCheckBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                textField.setVisible(true);
                textField.setManaged(true);
                login_pwd.setVisible(false);
                login_pwd.setManaged(false);
            } else {
                login_pwd.setVisible(true);
                login_pwd.setManaged(true);
                textField.setVisible(false);
                textField.setManaged(false);
            }
        });
    }
}
