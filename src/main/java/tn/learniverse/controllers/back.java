package tn.learniverse.controllers;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class back implements Initializable {
    public Label navUsernameLabel;
    public TextField searchField;
    public Label FirstLetter;
    public Circle circleProfile;
    public Button logoutButton;
    public Button Profilebtn;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            ImageView imageView = new ImageView();
            Image image = new Image("file:///C:/wamp64/www/images/icon/logout.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.logoutButton.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        try {
            ImageView imageView = new ImageView();
            Image image = new Image("file:///C:/wamp64/www/images/icon/profile.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.Profilebtn.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        if(Session.getCurrentUser()!=null){
            this.navUsernameLabel.setText(Session.getCurrentUser().getNom());
            this.FirstLetter.setText(Session.getCurrentUser().getNom().toUpperCase().substring(0, 1));
            Random random = new Random();
            Color randomColor = Color.rgb(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
            );
            circleProfile.setFill(randomColor);
        }
    }
    public void usersButton(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/usersBack.fxml");
    }

    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.redirect(actionEvent,"/fxml/user/login.fxml");
    }

    public void Profile(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/ProfileBack.fxml");
    }
    public void Comp(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/backoffice_competitions.fxml");
    }

    public void ToReclamations(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Reclamation/DisplayReclamationBack.fxml");
    }

    public void ToForums(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/BackForum.fxml");
    }

    public void ToCourses(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/BackCourses.fxml");
    }

    public void ToOffers(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/BackCourses.fxml");
    }
}
