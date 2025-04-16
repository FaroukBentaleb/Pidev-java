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
    public void initialize() {
        categorieU.getItems().addAll(
                "Programming & Tech",
                "Digital Marketing",
                "Health & Fitness",
                "Product Design"
        );
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
                showAlert("Erreur", "Veuillez remplir tous les champs obligatoires", Alert.AlertType.ERROR);
                return;
            }

            // Mettre à jour l'objet Poste
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
            showAlert("Succès", "Poste modifié avec succès!", Alert.AlertType.INFORMATION);

            // Fermer la fenêtre et rafraîchir la liste
            if (refreshCallback != null) {
                refreshCallback.run();
            }
            ((Stage) titreU.getScene().getWindow()).close();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
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
}