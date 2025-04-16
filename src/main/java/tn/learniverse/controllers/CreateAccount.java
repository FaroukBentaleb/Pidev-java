package tn.learniverse.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.learniverse.entities.user;
import tn.learniverse.services.UserService;
import javafx.scene.control.ChoiceBox;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CreateAccount implements Initializable {

    private final UserService userService = new UserService();
    public Button login_btn;

    @FXML
    private TextField signup_email;

    @FXML
    private TextField signup_family_name;

    @FXML
    private TextField signup_name;

    @FXML
    private ChoiceBox<String> signup_role;

    @FXML
    private TextField signup_conf_pwd;

    @FXML
    private TextField signup_pwd;

    @FXML
    void Signup_btn(ActionEvent event) {
        if (signup_pwd.getText().equals(signup_conf_pwd.getText())){
            try {
                userService.CreateAccount(new user(signup_name.getText(),signup_family_name.getText(),signup_email.getText(),signup_role.getValue(),signup_pwd.getText()));
            }
            catch (SQLException e){;
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error creating account");
                alert.setContentText("Something went wrong");
                alert.showAndWait();
            }
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Authentication Error");
            alert.setHeaderText("Password Mismatch");
            alert.setContentText("The passwords you entered do not match. Please try again.");
            alert.showAndWait();

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        signup_role.getItems().addAll("Student", "Insctructor", "Admin");
        signup_role.setValue("Student");
    }

    public void Login_btn(ActionEvent actionEvent) throws IOException {
        Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/Login.fxml"));
        Scene scene = new Scene(dashboardRoot);
        BufferedImage event = null;
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
