package tn.learniverse.controllers;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import tn.learniverse.entities.Poste;
import tn.learniverse.services.PosteService;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.stage.FileChooser;
import tn.learniverse.tests.MainFX;

import static tn.learniverse.entities.Poste.*;

public class AjouterPosteController {
    private final PosteService ps= new PosteService();
    @FXML
    private TextArea contenuA;
    @FXML
    private TextField titreA;
    @FXML
    private Label labelimg;
    private File selectedFile;
    private String imagePath;
    @FXML
    private ComboBox<String> categorieA;
    @FXML
    private Label titreErrorLabel;
    @FXML
    private Label contenuErrorLabel;
    @FXML
    private Button postButton;
    private boolean isTitreValid = false;
    private boolean isContenuValid = false;


    private Runnable refreshCallback;

    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }




    @FXML
    public void initialize() {
        categorieA.getItems().addAll(
                "Programming & Tech",
                "Digital Marketing",
                "Health & Fitness",
                "Product Design"
        );
        setupValidationListeners();
        postButton.setDisable(true);
    }

    @FXML
    void ajouter(ActionEvent event) throws IOException {
        Poste poste = new Poste(contenuA.getText(), titreA.getText(), categorieA.getValue());
        poste.setPhoto(imagePath); // affecte le chemin de la photo
        ps.ajouter(poste);

        if (refreshCallback != null) {
            refreshCallback.run();
        }
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void ajouterImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            labelimg.setText(selectedFile.getName());

            File destinationDir = new File("C:/wamp64/www/images");
            if (!destinationDir.exists()) {
                destinationDir.mkdirs();
            }
            File destinationFile = new File(destinationDir, selectedFile.getName());

            try {
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imagePath = "images/" + selectedFile.getName();
            } catch (IOException e) {
                e.printStackTrace();
                labelimg.setText("Erreur lors de la copie");
            }
        } else {
            labelimg.setText("Aucun fichier sélectionné");
        }
    }
    private void setupValidationListeners() {
        titreA.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                titreErrorLabel.setText("Le titre ne peut pas être vide");
                isTitreValid = false;
            } else if (newValue.length() > 10) {
                titreErrorLabel.setText("Le titre ne peut pas dépasser 10 caractères");
                isTitreValid = false;
            } else {
                titreErrorLabel.setText("");
                isTitreValid = true;
            }
            updatePostButtonState();
        });
        contenuA.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                contenuErrorLabel.setText("Le contenu ne peut pas être vide");
                isContenuValid = false;
            } else if (newValue.length() > 10) {
                contenuErrorLabel.setText("Le contenu ne peut pas dépasser 10 caractères");
                isContenuValid = false;
            } else {
                contenuErrorLabel.setText("");
                isContenuValid = true;
            }
            updatePostButtonState();
        });
        categorieA.valueProperty().addListener((observable, oldValue, newValue) -> {
            updatePostButtonState();
        });
    }


    private void updatePostButtonState() {
        boolean allFieldsValid = isTitreValid && isContenuValid && (categorieA.getValue() != null);
        postButton.setDisable(!allFieldsValid);

        if (allFieldsValid) {
            postButton.getStyleClass().removeAll("post-btn-disabled");
            postButton.getStyleClass().add("post-btn-enabled");
        } else {
            postButton.getStyleClass().removeAll("post-btn-enabled");
            postButton.getStyleClass().add("post-btn-disabled");
        }
    }
}
