package tn.learniverse.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.net.URL;
import java.util.ResourceBundle;

public class homePage implements Initializable {
    public Label role;
    public TextField searchField;
    public Label greetings;
    public ImageView UserPicture;
    public Label usernameLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("in");
        try {
            if(Session.getCurrentUser()==null){
                ActionEvent event = new ActionEvent();
                Navigator.redirect(event,"/fxml/user/Login.fxml");
            }
            else{
                this.usernameLabel.setText(Session.getCurrentUser().getNom());
                this.role.setText(Session.getCurrentUser().getRole());
                String picturePath = Session.getCurrentUser().getPicture();
                Image image;

                if (picturePath != null) {
                    image = new Image("file:///" + picturePath.replace("\\", "/"), 50, 50, false, false);
                    if(image.isError()){
                        image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 50, 50, false, false);
                    }
                } else {
                    image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 50, 50, false, false);
                }

                this.UserPicture.setImage(image);
                Circle clip = new Circle(25, 25, 25);
                this.UserPicture.setClip(clip);
                this.greetings.setText("Welcome back, " + Session.getCurrentUser().getNom());
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("out");
    }

    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.showAlert(Alert.AlertType.INFORMATION,"See you soon ","You are going to logout");
        Navigator.redirect(actionEvent,"/fxml/user/Login.fxml");
    }

    public void Profile(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/Profile.fxml");
    }
}
