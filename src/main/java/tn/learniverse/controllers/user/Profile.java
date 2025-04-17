package tn.learniverse.controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class Profile implements Initializable {
    public TextField linkedinField;
    public TextField instagramField;
    public TextField facebookField;
    public Label pictureFileLabel;
    public Label resumeFileLabel;
    public TextArea descriptionArea;
    public TextField experienceField;
    public TextField jobTitleField;
    public DatePicker birthDatePicker;
    public TextField phoneField;
    public TextField emailField;
    public TextField familyNameField;
    public TextField nameField;
    public Label role;
    public HBox verifiedBadge;
    public Label usernameLabel;
    public ImageView profileImage;
    public Label profileNameLabel;
    public TextField searchField;
    public ImageView UserPicture;
    public Button saveProfilebtn;
    public Label nameErrorLabel;
    public Label familyNameErrorLabel;
    public Label emailErrorLabel;
    public Label phoneErrorLabel;
    public Label jobTitleErrorLabel;
    public Label experienceErrorLabel;
    public Label descriptionErrorLabel;
    public Label linkedinErrorLabel;
    public Label instagramErrorLabel;
    public Label facebookErrorLabel;
    public Label birthDateErrorLabel;
    public Button btnUpload;
    public Label roleLabel;


    UserService userService = new UserService();

    public void chooseResumeFile(ActionEvent actionEvent) {
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
                    UserPicture.setImage(image);
                    pictureFileLabel.setText(destinationPath);
                    Session.getCurrentUser().setPicture(destinationPath);

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
    }

    public void saveProfile(ActionEvent actionEvent) {
        try {
            if(Session.getCurrentUser()!=null){
                User usr = new User(
                        this.nameField.getText(),
                        this.familyNameField.getText(),
                        this.emailField.getText(),
                        Session.getCurrentUser().getRole(), // role
                        null, // mdp
                        (this.birthDatePicker.getValue() != null) ? this.birthDatePicker.getValue().toString() : null,
                        Integer.parseInt(this.phoneField.getText()),
                        null,
                        this.descriptionArea.getText(),
                        Integer.parseInt(this.experienceField.getText()),
                        this.jobTitleField.getText(),
                        this.resumeFileLabel.getText(),
                        this.pictureFileLabel.getText(),
                        this.facebookField.getText(),
                        this.instagramField.getText(),
                        this.linkedinField.getText(),
                        Session.getCurrentUser().isVerified(),
                        Session.getCurrentUser().getLogs(),
                        Session.getCurrentUser().getBan()
                );
                userService.ModifyAccount(usr);
                Session.setCurrentUser(usr);
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

    public void cancelChanges(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent, "/fxml/homePage.fxml");

    }
    public void deleteAccount(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Do you really want to delete this account?" +
                "This action cannot be undone");
        alert.setContentText("Are you sure you want to delete this account?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userService.DeleteAccount(Session.getCurrentUser());
            Navigator.redirect(actionEvent, "/fxml/user/Login.fxml");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupRealTimeValidation();
        System.out.println("in!!");
        birthDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isAfter(LocalDate.of(2010, 1, 1))) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;"); // light red
                }
            }
        });
        birthDatePicker.setValue(LocalDate.of(2010, 1, 1));
        try{
            if(Session.getCurrentUser()!=null){
                User usr = Session.getCurrentUser();
                if (usr.getNom() != null) { this.nameField.setText(usr.getNom()); }
                if (usr.getPrenom() != null) { this.familyNameField.setText(usr.getPrenom()); }
                if (usr.getEmail() != null) { this.emailField.setText(usr.getEmail()); }
                if (usr.getTel() != 0) { this.phoneField.setText(Integer.toString(usr.getTel())); }
                if (usr.getDateDeNaissance() != null) { this.birthDatePicker.setValue(LocalDate.parse(usr.getDateDeNaissance())); }
                if (usr.getJob() != null) { this.jobTitleField.setText(usr.getJob()); }
                if (usr.getExperience() != 0) { this.experienceField.setText(Integer.toString(usr.getExperience())); }
                if (usr.getDescription() != null) { this.descriptionArea.setText(usr.getDescription()); }
                if (usr.getLinkedinLink() != null) { this.linkedinField.setText(usr.getLinkedinLink()); }
                if (usr.getInstagramLink() != null) { this.instagramField.setText(usr.getInstagramLink()); }
                if (usr.getFacebookLink() != null) { this.facebookField.setText(usr.getFacebookLink()); }
                if (usr.getPicture() != null) { this.pictureFileLabel.setText(usr.getPicture()); }
                if (usr.getResume() != null) { this.resumeFileLabel.setText(usr.getResume()); }
                if (usr.getNom() != null && usr.getPrenom() != null) {
                    String fullName = usr.getNom();
                    this.usernameLabel.setText(fullName);
                    this.profileNameLabel.setText(fullName);
                }
                if (usr.getRole() != null) {
                    this.role.setText(usr.getRole());
                    this.roleLabel.setText(usr.getRole() + " . "+ "Member since January 2023");
                }
                this.profileNameLabel.setText(usr.getNom());
                String picturePath = usr.getPicture();
                Image image;
                if (picturePath != null) {
                    image = new Image("file:///" + picturePath.replace("\\", "/"), 100, 100, false, true);
                    if(image.isError()){
                        image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 100, 100, false, true);
                        pictureFileLabel.setText("No given picture");
                    }
                    else{
                        pictureFileLabel.setText(image.getUrl());
                    }
                }
                else {
                    image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 100, 100, false, true);
                    pictureFileLabel.setText("No given picture");
                }
                this.profileImage.setImage(image);
                Circle clip = new Circle(50, 50, 50);
                this.profileImage.setClip(clip);
                this.UserPicture.setImage(image);
                Circle clip2 = new Circle(25, 25, 25);
                this.UserPicture.setClip(clip2);
                verifiedBadge.setVisible(usr.isVerified());
            }
            else{
                ActionEvent event = new ActionEvent();
                Navigator.redirect(event, "/fxml/user/Login.fxml");
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("out!!");
    }

    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.showAlert(Alert.AlertType.INFORMATION,"See you soon ","You are going to logout");
        Navigator.redirect(actionEvent,"/fxml/user/Login.fxml");
    }
    private void setupRealTimeValidation() {
        saveProfilebtn.setDisable(true);
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().length() < 2) {
                nameField.setStyle("-fx-border-color: red;");
                nameErrorLabel.setStyle("-fx-color: red;");
                nameErrorLabel.setText("Name must be at least 2 characters.");
                saveProfilebtn.setDisable(true);
            } else {
                nameField.setStyle(null);
                nameErrorLabel.setText("");
                saveProfilebtn.setDisable(false);
            }
        });

        familyNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().length() < 2) {
                familyNameField.setStyle("-fx-border-color: red;");
                familyNameErrorLabel.setText("Family name must be at least 2 characters.");
                saveProfilebtn.setDisable(true);
            } else {
                familyNameField.setStyle(null);
                familyNameErrorLabel.setText("");
                saveProfilebtn.setDisable(false);
            }
        });

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                emailField.setStyle("-fx-border-color: red;");
                emailErrorLabel.setText("Enter a valid email address.");
                saveProfilebtn.setDisable(true);
            } else {
                emailField.setStyle(null);
                emailErrorLabel.setText("");
                saveProfilebtn.setDisable(false);
            }
        });

        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("^\\+?[0-9]{6,15}$")) {
                phoneField.setStyle("-fx-border-color: red;");
                phoneErrorLabel.setText("Enter a valid phone number.");
                saveProfilebtn.setDisable(true);
            } else {
                phoneField.setStyle(null);
                phoneErrorLabel.setText("");
                saveProfilebtn.setDisable(false);
            }
        });

        jobTitleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().length() == 1) {
                jobTitleField.setStyle("-fx-border-color: red;");
                jobTitleErrorLabel.setText("Job title cannot be empty.");
                saveProfilebtn.setDisable(true);
            } else {
                jobTitleField.setStyle(null);
                jobTitleErrorLabel.setText("");
                saveProfilebtn.setDisable(false);
            }
        });

        experienceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("\\d+")) {
                experienceField.setStyle("-fx-border-color: red;");
                experienceErrorLabel.setText("Experience must be a number.");
                saveProfilebtn.setDisable(true);
            } else {
                experienceField.setStyle(null);
                experienceErrorLabel.setText("");
                saveProfilebtn.setDisable(false);
            }
        });

        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && newVal.trim().length() < 10) {
                descriptionArea.setStyle("-fx-border-color: red;");
                descriptionErrorLabel.setText("Description must be at least 10 characters.");
                saveProfilebtn.setDisable(true);
            } else {
                descriptionArea.setStyle(null);
                descriptionErrorLabel.setText("");
                saveProfilebtn.setDisable(false);
            }
        });

        linkedinField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty() && !newVal.startsWith("https://www.linkedin.com/")) {
                linkedinField.setStyle("-fx-border-color: red;");
                linkedinErrorLabel.setText("Must start with https://www.linkedin.com/");
                saveProfilebtn.setDisable(true);
            } else {
                linkedinField.setStyle(null);
                linkedinErrorLabel.setText("");
                saveProfilebtn.setDisable(false);
            }
        });

        instagramField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty() && !newVal.startsWith("https://www.instagram.com/")) {
                instagramField.setStyle("-fx-border-color: red;");
                instagramErrorLabel.setText("Must start with https://www.instagram.com/");
                saveProfilebtn.setDisable(true);
            } else {
                instagramField.setStyle(null);
                instagramErrorLabel.setText("");
                saveProfilebtn.setDisable(false);
            }
        });

        facebookField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty() && !newVal.startsWith("https://www.facebook.com/")) {
                facebookField.setStyle("-fx-border-color: red;");
                facebookErrorLabel.setText("Must start with https://www.facebook.com/");
                saveProfilebtn.setDisable(true);
            } else {
                facebookField.setStyle(null);
                facebookErrorLabel.setText("");
                saveProfilebtn.setDisable(false);
            }
        });

        birthDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            LocalDate maxDate = LocalDate.of(2010, 1, 1);
            if (newVal != null && newVal.isAfter(maxDate)) {
                birthDatePicker.setStyle("-fx-border-color: red;");
                birthDateErrorLabel.setText("Birth date must be on or before 01/01/2010.");
                saveProfilebtn.setDisable(true);
            } else {
                birthDatePicker.setStyle(null);
                birthDateErrorLabel.setText("");
                saveProfilebtn.setDisable(false);
            }
        });
    }


}
