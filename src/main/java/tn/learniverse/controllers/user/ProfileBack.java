package tn.learniverse.controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import tn.learniverse.entities.User;
import tn.learniverse.services.UserService;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Random;
import java.util.ResourceBundle;

public class ProfileBack implements Initializable {
    public Button Profilebtn;
    public Label FirstLetter;
    public Circle circleProfile;
    public Button logoutButton;

    public ImageView profileImage;
    public Label profileNameLabel;
    public Label navUsernameLabel;

    public TextField firstNameField;
    public TextField emailField;
    public TextField lastNameField;
    public TextField phoneField;
    public Label pictureFileLabel;
    public Button btnUpload;
    public Label phoneFieldErrorLabel;
    public Label lastNameFieldErrorLabel;
    public Label emailFieldErrorLabel;
    public Label firstNameErrorLabel;

    public void Logout(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/Login.fxml");
    }

    public void OnDashboard(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/Back.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupRealTimeValidation();
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
        Profilebtn.setDisable(true);
        if(Session.getCurrentUser()!=null){
            if(Session.getCurrentUser().getPicture()!=null){
                this.pictureFileLabel.setText(Session.getCurrentUser().getPicture());
            }
            this.navUsernameLabel.setText(Session.getCurrentUser().getNom());
            this.profileNameLabel.setText(Session.getCurrentUser().getNom());
            this.emailField.setText(Session.getCurrentUser().getEmail());
            this.FirstLetter.setText(Session.getCurrentUser().getNom().toUpperCase().substring(0, 1));
            this.phoneField.setText(Integer.toString(Session.getCurrentUser().getTel()));
            Random random = new Random();
            Color randomColor = Color.rgb(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
            );
            circleProfile.setFill(randomColor);

            String picturePath = Session.getCurrentUser().getPicture();
            Image image;
            if (picturePath != null) {
                image = new Image("file:///" + picturePath.replace("\\", "/"), 100, 100, false, true);
                if(image.isError()){
                    image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 100, 100, false, true);
                }
            }
            else {
                image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 100, 100, false, true);
            }
            this.profileImage.setImage(image);
            Circle clip = new Circle(50, 50, 50);
            this.profileImage.setClip(clip);
        }
    }

    public void SaveInfos(ActionEvent actionEvent) {
        try {
            UserService userService = new UserService();
            if(Session.getCurrentUser()!=null){
                User usr = new User();
                usr = Session.getCurrentUser();
                usr.setNom(firstNameField.getText());
                usr.setPrenom(lastNameField.getText());
                usr.setTel(Integer.parseInt(phoneField.getText()));
                usr.setEmail(emailField.getText());
                System.out.println(usr);
                userService.ModifyAccount(usr);
                Session.setCurrentUser(usr);
                this.profileNameLabel.setText(usr.getNom());
                this.navUsernameLabel.setText(usr.getNom());
                this.FirstLetter.setText(usr.getNom().toUpperCase().substring(0, 1));

                Navigator.showAlert(Alert.AlertType.INFORMATION, "Profile Updated", "Your information has been updated successfully.");
            }
            else{
                Navigator.redirect(actionEvent, "fxml/user/Login.fxml");
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void chooseProfilePicture(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(btnUpload.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                String newFileName = "user_" + System.currentTimeMillis() + extension;
                String destinationPath = "C:/wamp64/www/images/users/" + newFileName;

                File destinationFile = new File(destinationPath);
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Image image = new Image(destinationFile.toURI().toString());
                profileImage.setImage(image);
                pictureFileLabel.setText(destinationPath);
                Session.getCurrentUser().setPicture(destinationPath);

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    private void setupRealTimeValidation() {
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().length() < 2) {
                firstNameField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 10; -fx-border-radius: 8;-fx-border-color: red;");
                firstNameErrorLabel.setText("Name must be at least 2 characters.");
                firstNameErrorLabel.setStyle("-fx-text-fill: red;");
            } else {
                firstNameField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 10; -fx-border-radius: 8;-fx-border-color: #e1e4e8;");
                firstNameErrorLabel.setText("");
            }
        });

        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().length() < 2) {
                lastNameField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 10; -fx-border-radius: 8;-fx-border-color: red;");
                lastNameFieldErrorLabel.setText("Family name must be at least 2 characters.");
                lastNameFieldErrorLabel.setStyle("-fx-text-fill: red;");
            } else {
                lastNameField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 10; -fx-border-radius: 8;-fx-border-color: #e1e4e8;");
                lastNameFieldErrorLabel.setText("");
            }
        });

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                emailField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 10; -fx-border-radius: 8;-fx-border-color: red;");
                emailFieldErrorLabel.setText("Enter a valid email address.");
                emailFieldErrorLabel.setStyle("-fx-text-fill: red;");
            } else {
                emailField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 10; -fx-border-radius: 8;-fx-border-color: #e1e4e8;");
                emailFieldErrorLabel.setText("");
            }
        });

        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && Integer.parseInt(newVal)!=0 && !newVal.matches("^\\+?[0-9]{6,15}$")) {
                phoneField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 10; -fx-border-radius: 8;-fx-border-color: red;");
                phoneFieldErrorLabel.setText("Enter a valid phone number.");
                phoneFieldErrorLabel.setVisible(true);
                phoneFieldErrorLabel.setStyle("-fx-text-fill: red;");
            } else {
                phoneField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 10; -fx-border-radius: 8;-fx-border-color: #e1e4e8;");
                phoneFieldErrorLabel.setText("");
            }
        });
    }

    public void Comp(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/backoffice_competitions.fxml");
    }
}
