package tn.learniverse.controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Settings implements Initializable {
    public ToggleButton ToggleTwoFA;
    public Button Settingsbtn;
    public Button logoutButton;
    public Label usernameLabel;
    public ImageView UserPicture;
    public Label role;
    public Button securityBtn;
    public ScrollPane settingsContentPane;

    public void LogsBtn(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent, "/fxml/user/LogsList.fxml");
    }

    public void TwoFAbtn(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent, "/fxml/user/2FA.fxml");
    }

    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.showAlert(Alert.AlertType.INFORMATION,"See you soon ","You are going to logout");
        Navigator.redirect(actionEvent,"/fxml/user/Login.fxml");
    }

    public void Settings(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent, "/fxml/user/Settings.fxml");
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/Profile.fxml"));
        try {
            Parent profileContent = loader.load();
            settingsContentPane.setContent(profileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            Image image = new Image("file:///C:/wamp64/www/images/icon/settings.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.Settingsbtn.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        try {
            if (Session.getCurrentUser() == null) {
                ActionEvent event = new ActionEvent();
                Navigator.redirect(event, "/fxml/user/Login.fxml");
            } else {
                this.usernameLabel.setText(Session.getCurrentUser().getNom());
                this.role.setText(Session.getCurrentUser().getRole());
                String picturePath = Session.getCurrentUser().getPicture();
                Image image;

                if (picturePath != null) {
                    image = new Image("file:///" + picturePath.replace("\\", "/"), 50, 50, false, false);
                    if (image.isError()) {
                        image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 50, 50, false, false);
                    }
                } else {
                    image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 50, 50, false, false);
                }

                this.UserPicture.setImage(image);
                Circle clip = new Circle(25, 25, 25);
                this.UserPicture.setClip(clip);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void pofile(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent, "/fxml/user/Profile.fxml");
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
        if(Session.getCurrentUser().getRole().equals("Student")){
            Navigator.redirect(actionEvent,"/SubscriptionCourses.fxml");
        }
        else{
            Navigator.redirect(actionEvent,"/MyCourses.fxml");
        }
    }

    public void ToOffers(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/OffreDisplay.fxml");

    }
}
