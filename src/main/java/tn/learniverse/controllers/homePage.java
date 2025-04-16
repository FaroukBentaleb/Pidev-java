package tn.learniverse.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.net.URL;
import java.util.ResourceBundle;

public class homePage implements Initializable {
    public Label username;
    public Label role;
    public TextField searchField;
    public Label greetings;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(Session.getCurrentUser()==null){
            ActionEvent event = new ActionEvent();
            Navigator.redirect(event,"/fxml/user/Login.fxml");
        }
        else{
            this.username.setText(Session.getCurrentUser().getNom() + " " + Session.getCurrentUser().getPrenom());
            this.role.setText(Session.getCurrentUser().getRole());
            System.out.println(Session.getCurrentUser());
            this.greetings.setText("Welcome back, " + Session.getCurrentUser().getNom());
        }
    }
}
