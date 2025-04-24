package tn.learniverse.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import tn.learniverse.entities.*;
import tn.learniverse.services.*;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

public class Login {
    public TextField login_email;
    public PasswordField login_pwd;

    public void Login_btn(ActionEvent actionEvent) {
        UserService userService = new UserService();
        User usr = new User();
        String login = login_email.getText();
        String pwd = login_pwd.getText();
        try {
            usr = userService.getUserByEmail(login_email.getText());
            if(usr == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("No User Found");
                alert.setContentText("The given email doesn't match any account!!");
                alert.showAndWait();
            }
            else{
                if(usr.getMdp().equals(pwd)) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setContentText("Connected successfully");
                    alert.showAndWait();
                    Session.setCurrentUser(usr);
                    Navigator.redirect(actionEvent,"/Profile.fxml");
                }
                else{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Password Incorrect");
                    alert.setContentText("Please try again!!");
                    alert.showAndWait();
                }
            }
        }
        catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("An error occured while trying to login");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void Signup_btn(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/CreateAccount.fxml");
    }
}
