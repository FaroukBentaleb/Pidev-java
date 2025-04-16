package tn.learniverse.controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import tn.learniverse.entities.User;
import tn.learniverse.services.UserService;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;
import java.net.URL;
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
    public HBox verifiedBadge;
    public Label usernameLabel;
    public ImageView profileImage;

    public void chooseResumeFile(ActionEvent actionEvent) {
    }

    public void chooseProfilePicture(ActionEvent actionEvent) {

    }

    public void saveProfile(ActionEvent actionEvent) {

    }

    public void cancelChanges(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent, "/fxml/home.fxml");

    }
    public void deleteAccount(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Do you really want to delete this account?" +
                "This action cannot be undone");
        alert.setContentText("Are you sure you want to delete this account?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            UserService userService = new UserService();
            userService.DeleteAccount(Session.getCurrentUser());
            Navigator.redirect(actionEvent, "/fxml/user/Login.fxml");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(Session.getCurrentUser()!=null){
            User usr = Session.getCurrentUser();
            this.nameField.setText(usr.getNom());
            this.familyNameField.setText(usr.getPrenom());
            this.emailField.setText(usr.getEmail());
            this.phoneField.setText(Integer.toString(usr.getTel()));
            if(usr.getDateDeNaissance() != null) {
                this.birthDatePicker.setValue(LocalDate.parse(usr.getDateDeNaissance()));
            }
            this.jobTitleField.setText(usr.getJob());
            this.experienceField.setText(Integer.toString(usr.getExperience()));
            this.descriptionArea.setText(usr.getDescription());
            this.linkedinField.setText(usr.getLinkedinLink());
            this.instagramField.setText(usr.getInstagramLink());
            this.facebookField.setText(usr.getFacebookLink());

            this.pictureFileLabel.setText(usr.getPicture());
            this.resumeFileLabel.setText(usr.getResume());

            this.usernameLabel.setText(usr.getNom() + " " + usr.getPrenom());
            if (usr.isVerified()) {
                verifiedBadge.setVisible(true);
            } else {
                verifiedBadge.setVisible(false);
            }
            if (usr.getPicture() != null && !usr.getPicture().isEmpty()) {
                Image image = new Image(usr.getPicture(), true);
                profileImage.setImage(image);
            }



        }
        else{
            ActionEvent event = new ActionEvent();
            Navigator.redirect(event, "/fxml/user/Login.fxml");
        }
    }
}
