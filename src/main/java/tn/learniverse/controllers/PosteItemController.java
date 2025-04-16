package tn.learniverse.controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.learniverse.entities.Poste;
import javafx.scene.control.Label;
import tn.learniverse.services.PosteService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Optional;

public class PosteItemController {

    private Runnable onDeleteCallback;
    private Poste poste;

    @FXML
    private Label auteurLabel;

    @FXML
    private Button deleteB;

    @FXML
    private Button updateB;


    @FXML
    private Label categorieLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label nbCommentairesLabel;

    @FXML
    private Label titreLabel;

    @FXML
    private Label contenuLabel;

    @FXML
    private ImageView photoImageView;

    public void setData(Poste poste, Runnable refreshCallback) {

        this.poste = poste;
        this.onDeleteCallback = refreshCallback;

        System.out.println("Poste ID: " + poste.getId());

        if (poste.getUser() != null) {
            auteurLabel.setText(poste.getUser().getPrenom() + " " + poste.getUser().getNom());
        }

        categorieLabel.setText(poste.getCategorie());
        dateLabel.setText(poste.getDatePost());
        nbCommentairesLabel.setText(poste.getNbCom() + " comments");
        titreLabel.setText(poste.getTitre());
        contenuLabel.setText(poste.getContenu());

        if (poste.getPhoto() != null && !poste.getPhoto().isEmpty()) {
            try {
                // Chemin complet vers l'image
                File imageFile = new File("C:/wamp64/www/" + poste.getPhoto());

                if (imageFile.exists()) {
                    String photoPath = imageFile.toURI().toString();
                    System.out.println("URL de l'image test : " + photoPath);

                    Image image = new Image(photoPath, true); // Chargement asynchrone
                    photoImageView.setImage(image);
                } else {
                    System.out.println("ERREUR: Fichier image non trouvé à : " + imageFile.getAbsolutePath());
                    photoImageView.setVisible(false);}


            } catch (Exception e) {
                System.out.println("Erreur chargement image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer ce post ?");
            confirm.setContentText("Êtes-vous sûr de vouloir supprimer : " + poste.getTitre());

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                new PosteService().supprimer(poste.getId());

                if (onDeleteCallback != null) {
                    onDeleteCallback.run(); // Rafraîchir la liste des posts
                }
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression : " + e.getMessage()).show();
        }
    }


    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            // Charge le FXML en utilisant le bon chemin
            FXMLLoader loader = new FXMLLoader();
            InputStream fxmlStream = getClass().getResourceAsStream("/ModifierPoste.fxml");

            if (fxmlStream == null) {
                throw new IOException("Fichier FXML non trouvé");
            }

            Parent root = loader.load(fxmlStream);

            ModifierPosteController controller = loader.getController();
            controller.setPosteData(poste, onDeleteCallback);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier le Poste");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir l'éditeur");
            alert.setContentText("Erreur lors du chargement de l'interface: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void ouvrirCommentaires(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherCommentaires.fxml"));
            Parent root = loader.load();

            AfficherCommentairesController controller = loader.getController();
            controller.setPoste(poste, onDeleteCallback); // Passer aussi le callback

            Stage stage = new Stage();
            stage.setTitle("Commentaires - " + poste.getTitre());
            stage.setScene(new Scene(root, 800, 600)); // Taille appropriée
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir les commentaires");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public Button getDeleteB() {
        return deleteB;
    }

    public Button getUpdateB() {
        return updateB;
    }



}
