package tn.learniverse.controllers;

import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.event.ActionEvent;
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
    private ComboBox<String> categorieA;

    @FXML
    public void initialize() {
        categorieA.getItems().addAll(
                "Programming & Tech",
                "Digital Marketing",
                "Health & Fitness",
                "Product Design"
        );
    }

    @FXML
    private TextArea contenuA;

    @FXML
    private TextField titreA;



    @FXML
    void ajouter(ActionEvent event) throws IOException {
        Poste poste =new Poste(contenuA.getText(), titreA.getText(),categorieA.getValue());
        poste.setPhoto(imagePath); // affecte le chemin de la photo
        ps.ajouter(poste);
        MainFX.showAfficherPostesScene();
    }


    @FXML
    private Label labelimg;
    private File selectedFile;
    private String imagePath;

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

            // Dossier cible
            File destinationDir = new File("C:/wamp64/www/images");
            if (!destinationDir.exists()) {
                destinationDir.mkdirs();
            }

            // Fichier destination avec même nom
            File destinationFile = new File(destinationDir, selectedFile.getName());

            try {
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imagePath = "images/" + selectedFile.getName(); // Chemin à enregistrer dans la base


            } catch (IOException e) {
                e.printStackTrace();
                labelimg.setText("Erreur lors de la copie");
            }
        } else {
            labelimg.setText("Aucun fichier sélectionné");
        }
    }


}
