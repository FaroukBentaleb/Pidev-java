package tn.learniverse.controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import tn.learniverse.entities.Logs;
import tn.learniverse.services.LogsService;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class LogsList implements Initializable {
    @FXML
    private VBox logsContainer;
    public ToggleButton ToggleTwoFA;
    public Button Settingsbtn;
    public Button logoutButton;
    public Label usernameLabel;
    public ImageView UserPicture;
    public Label role;
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
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        LogsService logsService = new LogsService();
        List<Logs> logs = logsService.getLogsByUserId(Session.getCurrentLog().getUserId());
        for (Logs log : logs) {
            logsContainer.getChildren().add(createLogCard(log));
        }
    }
    private Node createLogCard(Logs log) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");

        // Top status bar
        HBox statusBar = new HBox();
        statusBar.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel;
        System.out.println("Sesssion: " + Session.getCurrentLog().getId());
        System.out.println("Log: " + log.getId());
        if (Session.getCurrentLog().getId()==log.getId()) {
            statusLabel = new Label("Current Session");
            statusLabel.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 12;"
                    + "-fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-padding: 5 10;");
        } else {
            statusLabel = new Label("Previous Session");
            statusLabel.setStyle("-fx-background-color: #FFF3E0; -fx-background-radius: 12;"
                    + "-fx-text-fill: #FB8C00; -fx-font-weight: bold; -fx-padding: 5 10;");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label activityLabel = new Label("Last activity: " + log.getTimestamp());
        activityLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        statusBar.getChildren().addAll(statusLabel, spacer, activityLabel);

        // Device info with emoji
        HBox deviceInfo = new HBox(15);
        deviceInfo.setAlignment(Pos.CENTER_LEFT);

        Label emoji = new Label("💻");
        emoji.setStyle("-fx-font-size: 24px; -fx-text-fill: #1976D2;");

        StackPane emojiPane = new StackPane(emoji);
        emojiPane.setMinSize(50, 50);
        emojiPane.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 25;");

        VBox deviceLabels = new VBox(3);
        Label deviceModel = new Label(log.getDeviceModel());
        deviceModel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label deviceType = new Label(log.getDeviceType());
        deviceType.setStyle("-fx-text-fill: #666;");
        deviceLabels.getChildren().addAll(deviceModel, deviceType);

        deviceInfo.getChildren().addAll(emojiPane, deviceLabels);

        // Log details
        System.out.println("Log: " +log.getAction());
        VBox details = new VBox(8);
        details.setStyle("-fx-padding: 10 0 0 0;");
        details.getChildren().addAll(
                makeDetailRow("Action: ", log.getAction()),
                makeDetailRow("Location: ", log.getLocation()),
                makeDetailRow("Timestamp: ", log.getTimestamp().toString())
        );

        // Action button
        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setSpacing(10);
        actions.setStyle("-fx-padding: 10 0 0 0;");

        Button stopSessionBtn = new Button("Stop session");
        stopSessionBtn.setStyle("-fx-background-color: #FCE3E3; -fx-text-fill: #D11919; -fx-background-radius: 4;");
        stopSessionBtn.setCursor(Cursor.HAND);

        stopSessionBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Session Termination");
            alert.setHeaderText("Do you really want to stop this session?");
            alert.setContentText("This action cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                LogsService logsService = new LogsService();
                System.out.println("Trying to delete log with ID: " + log.getId());
                boolean success = logsService.deleteLog(log.getId());
                if (success) {
                    logsContainer.getChildren().remove(card);
                } else {
                    System.out.println("Failed to delete log from database.");
                }
            }
        });

        actions.getChildren().add(stopSessionBtn);

        card.getChildren().addAll(statusBar, deviceInfo, details, actions);
        return card;
    }


    private HBox makeDetailRow(String labelText, String value) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");

        Label valueLabel = new Label((value == null || value.trim().isEmpty()) ? "N/A" : value);
        valueLabel.setStyle("-fx-text-fill: #333;");

        HBox row = new HBox(5, label, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }


    public void refreshLogsBtn(ActionEvent actionEvent) {
        loadLogs();
    }
    private void loadLogs() {
        logsContainer.getChildren().clear();

        LogsService logsService = new LogsService();
        List<Logs> logs = logsService.getLogsByUserId(Session.getCurrentLog().getUserId());

        for (Logs log : logs) {
            logsContainer.getChildren().add(createLogCard(log));
        }
    }

    public void TwoFABtn(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/2FA.fxml");
    }

    public void Profile(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/Settings.fxml");
    }

    public void Settings(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/Settings.fxml");
    }

    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.showAlert(Alert.AlertType.INFORMATION,"See you soon ","You are going to logout");
        Navigator.redirect(actionEvent,"/fxml/user/Login.fxml");
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
        Navigator.redirect(actionEvent,"/CoursesView.fxml");
    }

    public void ToOffers(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/CoursesView.fxml");

    }
}
