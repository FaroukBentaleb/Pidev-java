package tn.learniverse.controllers.Reclamation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.learniverse.entities.Reclamation;
import tn.learniverse.entities.User;
import tn.learniverse.services.ReclamationService;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AjouterReclamation implements Initializable {

    public Button btnUpload;
    @FXML
    private Button ReclamationAjouter;

    @FXML
    private TextField reclamationContenu;

    @FXML
    private ImageView reclamationImg;

    @FXML
    private TextField reclamationtTitre;

    @FXML
    private TextField filePathTextField;

    @FXML
    private Label titreErrorLabel;

    @FXML
    private Label contenuErrorLabel;

    @FXML
    private Label fichierErrorLabel;

    private ReclamationService reclamationService = new ReclamationService();
    private DisplayReclamations displayReclamationsController;

    // Pattern to check if text contains only letters and spaces
    private final Pattern LETTERS_ONLY_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s]+$");

    public void setDisplayReclamationsController(DisplayReclamations controller) {
        this.displayReclamationsController = controller;
    }

    @FXML
    public void ajouterReclamation(ActionEvent event) {
        // Reset error messages
        resetErrorMessages();

        // Validate fields
        boolean isValid = validateAllFields();

        if (isValid) {
            String titre = reclamationtTitre.getText();
            String contenu = reclamationContenu.getText();
            String fichier = filePathTextField.getText();

            Reclamation reclamation = new Reclamation();
            reclamation.setTitre(titre);
            reclamation.setContenu(contenu);

            User user = new User();
            user.setId(3);

            try {
                reclamationService.ajouter(reclamation, user);
                Stage currentStage = (Stage) ReclamationAjouter.getScene().getWindow();
                currentStage.close();
                if (displayReclamationsController != null) {
                    displayReclamationsController.initialize();
                }
                System.out.println("Réclamation ajoutée avec succès !");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validateAllFields() {
        boolean isTitreValid = validateTitre();
        boolean isContenuValid = validateContenu();

        return isTitreValid && isContenuValid;
    }

    private boolean validateTitre() {
        String titre = reclamationtTitre.getText().trim();

        if (titre.isEmpty()) {
            titreErrorLabel.setText("Le titre est obligatoire");
            titreErrorLabel.setTextFill(Color.RED);
            reclamationtTitre.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
            return false;
        } else if (titre.length() < 3) {
            titreErrorLabel.setText("Le titre doit contenir au moins 3 caractères");
            titreErrorLabel.setTextFill(Color.RED);
            reclamationtTitre.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
            return false;
        } else if (!LETTERS_ONLY_PATTERN.matcher(titre).matches()) {
            titreErrorLabel.setText("Le titre ne doit contenir que des lettres");
            titreErrorLabel.setTextFill(Color.RED);
            reclamationtTitre.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
            return false;
        }

        titreErrorLabel.setText("");
        return true;
    }

    private boolean validateContenu() {
        String contenu = reclamationContenu.getText().trim();

        if (contenu.isEmpty()) {
            contenuErrorLabel.setText("Le contenu est obligatoire");
            contenuErrorLabel.setTextFill(Color.RED);
            reclamationContenu.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
            return false;
        } else if (contenu.length() < 10) {
            contenuErrorLabel.setText("Le contenu doit contenir au moins 10 caractères");
            contenuErrorLabel.setTextFill(Color.RED);
            reclamationContenu.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
            return false;
        }

        contenuErrorLabel.setText("");
        return true;
    }

    private void resetErrorMessages() {
        titreErrorLabel.setText("");
        contenuErrorLabel.setText("");
        fichierErrorLabel.setText("");
    }

    @FXML
    private void handleJoinFile() {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", ".png", ".jpg", ".jpeg", ".gif")
            );

            File selectedFile = fileChooser.showOpenDialog(btnUpload.getScene().getWindow());
            if (selectedFile != null) {
                try {
                    String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                    String newFileName = "user_" + System.currentTimeMillis() + extension;
                    String destinationPath = "C:/wamp64/www/images/reclamation/" + newFileName;

                    File destinationFile = new File(destinationPath);
                    Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Image image = new Image(destinationFile.toURI().toString());

                    filePathTextField.setText(destinationPath);
                    Session.getCurrentUser().setPicture(destinationPath);

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.reclamationImg.setImage(new Image("file:///C:/wamp64/www/images/logo/logo.png"));

        // Set up listeners for real-time validation
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        // Titre field listener
        reclamationtTitre.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                reclamationtTitre.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
                titreErrorLabel.setText("");
            } else if (newValue.trim().length() < 3) {
                reclamationtTitre.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
                titreErrorLabel.setText("Le titre doit contenir au moins 3 caractères");
                titreErrorLabel.setTextFill(Color.RED);
            } else if (!LETTERS_ONLY_PATTERN.matcher(newValue.trim()).matches()) {
                reclamationtTitre.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
                titreErrorLabel.setText("Le titre ne doit contenir que des lettres");
                titreErrorLabel.setTextFill(Color.RED);
            } else {
                reclamationtTitre.setStyle("-fx-border-color: green; -fx-border-radius: 8;");
                titreErrorLabel.setText("");
            }
        });

        // Contenu field listener
        reclamationContenu.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                reclamationContenu.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
                contenuErrorLabel.setText("");
            } else if (newValue.trim().length() < 10) {
                reclamationContenu.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
                contenuErrorLabel.setText("Le contenu doit contenir au moins 10 caractères");
                contenuErrorLabel.setTextFill(Color.RED);
            } else {
                reclamationContenu.setStyle("-fx-border-color: green; -fx-border-radius: 8;");
                contenuErrorLabel.setText("");
            }
        });
    }
}