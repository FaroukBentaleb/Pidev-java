package tn.learniverse.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import tn.learniverse.services.LogsService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class homePage implements Initializable {
    public Label role;
    public TextField searchField;
    public Label greetings;
    public ImageView UserPicture;
    public Label usernameLabel;
    public Button logoutButton;
    public Button Settingsbtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startSessionMonitor();
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
    public void Settings(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/Settings.fxml");
    }

    public void ToLogs(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/LogsList.fxml");
    }
    private Timeline sessionMonitor;

    private void startSessionMonitor() {
        sessionMonitor = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> {
                    LogsService logsService = new LogsService();
                    if (Session.getCurrentLog() != null) {
                        int currentLogId = Session.getCurrentLog().getId();
                        boolean exists = logsService.logExists(currentLogId);
                        if (!exists) {
                            // Stop the session monitor
                            sessionMonitor.stop();

                            Platform.runLater(() -> {
                                try {
                                    Navigator.showAlert(Alert.AlertType.WARNING, "Session expired", "You have been logged out.");
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/Login.fxml"));
                                    Parent root = loader.load();
                                    Stage stage = (Stage) Stage.getWindows().filtered(Window::isShowing).get(0);
                                    stage.setScene(new Scene(root));
                                    stage.setTitle("Login");
                                    stage.show();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            });
                        }
                    }
                })
        );
        sessionMonitor.setCycleCount(Timeline.INDEFINITE);
        sessionMonitor.play();
    }


}
