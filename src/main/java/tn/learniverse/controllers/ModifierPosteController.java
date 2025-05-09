package tn.learniverse.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.learniverse.entities.Poste;
import tn.learniverse.services.PosteService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ModifierPosteController {

    @FXML private ComboBox<String> categorieU;
    @FXML private TextField titreU;
    @FXML private TextArea contenuU;
    @FXML private Label labelimgU;
    private Poste currentPoste;
    private Runnable refreshCallback;
    private String newImagePath;
    @FXML
    private Label titreErrorLabel;
    @FXML
    private Label contenuErrorLabel;
    @FXML
    private boolean isTitreValid = false;
    private boolean isContenuValid = false;
    @FXML
    private Button postButton;


    @FXML
    public void initialize() {
        categorieU.getItems().addAll(
                "Programming & Tech",
                "Digital Marketing",
                "Health & Fitness",
                "Product Design"
        );

        setupValidationListenersU();
        postButton.setDisable(true);
    }


    public void setPosteData(Poste poste, Runnable callback) {
        this.currentPoste = poste;
        this.refreshCallback = callback;

        // Pré-remplir les champs avec les données actuelles du poste
        titreU.setText(poste.getTitre());
        contenuU.setText(poste.getContenu());
        categorieU.setValue(poste.getCategorie());

        // Afficher le nom du fichier image s'il existe
        if (poste.getPhoto() != null && !poste.getPhoto().isEmpty()) {
            labelimgU.setText(poste.getPhoto().substring(poste.getPhoto().lastIndexOf("/") + 1));
        }
    }

    @FXML
    private void modifierImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Copier l'image vers le dossier www/images
                Path source = selectedFile.toPath();
                Path targetDir = Paths.get("C:/wamp64/www/images");

                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }

                Path target = targetDir.resolve(selectedFile.getName());
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

                newImagePath = "/images/" + selectedFile.getName();
                labelimgU.setText(selectedFile.getName());

            } catch (IOException e) {
                showAlert("Erreur", "Impossible de copier l'image: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void modifier() {
        try {
            // Validation des champs
            if (titreU.getText().isEmpty() || contenuU.getText().isEmpty() || categorieU.getValue() == null) {
                showAlert("Error", "Please fill in all required fields", Alert.AlertType.ERROR);
                return;
            }

            currentPoste.setTitre(titreU.getText());
            currentPoste.setContenu(contenuU.getText());
            currentPoste.setCategorie(categorieU.getValue());

            // Mettre à jour l'image si une nouvelle a été sélectionnée
            if (newImagePath != null) {
                currentPoste.setPhoto(newImagePath);
            }

            // Appeler le service de modification
            PosteService posteService = new PosteService();
            posteService.modifier(currentPoste);

            // Afficher un message de succès
            showAlert("Success", "Post successfully modified!", Alert.AlertType.INFORMATION);

            // Fermer la fenêtre et rafraîchir la liste
            if (refreshCallback != null) {
                refreshCallback.run();
            }
            ((Stage) titreU.getScene().getWindow()).close();

        } catch (Exception e) {
            showAlert("Erreur", "Error while editing: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupValidationListenersU() {
        titreU.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                titreErrorLabel.setText("Title cannot be empty");
                isTitreValid = false;
            } else if (newValue.length() > 20) {
                titreErrorLabel.setText("The Title cannot exceed 20 characters");
                isTitreValid = false;
            } else {
                titreErrorLabel.setText("");
                isTitreValid = true;
            }
            updatePostButtonStateU();
        });

        contenuU.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                contenuErrorLabel.setText("Content cannot be empty");
                isContenuValid = false;
            } else if (newValue.length() > 500) {
                contenuErrorLabel.setText("The Title cannot exceed 500 characters");
                isContenuValid = false;
            } else {
                contenuErrorLabel.setText("");
                isContenuValid = true;
            }
            updatePostButtonStateU();
        });

        // Validation de la catégorie
        categorieU.valueProperty().addListener((observable, oldValue, newValue) -> {
            updatePostButtonStateU();
        });
    }


    private void updatePostButtonStateU() {

        boolean allFieldsValid = isTitreValid && isContenuValid && (categorieU.getValue() != null);
        postButton.setDisable(!allFieldsValid);

        // Style optionnel
        if (allFieldsValid) {
            postButton.getStyleClass().removeAll("post-btn-disabled");
            postButton.getStyleClass().add("post-btn-enabled");
        } else {
            postButton.getStyleClass().removeAll("post-btn-enabled");
            postButton.getStyleClass().add("post-btn-disabled");
        }
    }
}