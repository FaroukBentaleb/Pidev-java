package tn.learniverse.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

public class SubscriptionCourses {
    public VBox MainContainer;
    public VBox MainListView;
    public Button logoutButton;
    public Button Settingsbtn;
    public Label role;
    public Label usernameLabel;
    public ImageView UserPicture;

    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.showAlert(Alert.AlertType.INFORMATION,"See you soon ","You are going to logout");
        Navigator.redirect(actionEvent,"/fxml/user/Login.fxml");
    }
    public void Settings(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/Settings.fxml");
    }

    public void Comp(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/competitions_list.fxml");

    }

    public void ToReclamaitons(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Reclamation/DisplayReclamations.fxml");
    }

    public void ToForum(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/AfficherPoste.fxml");
    }

    public void ToDiscover(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/CoursesView.fxml");
    }

    public void ToCourses(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/MyCourses.fxml");
    }

    public void ToOffers(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/CoursesView.fxml");

    }
}
