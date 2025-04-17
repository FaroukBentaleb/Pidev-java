package tn.learniverse.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.learniverse.entities.Commentaire;
import tn.learniverse.services.CommentaireService;

import java.util.Optional;

public class CommentaireItemController {
    @FXML private Label auteurLabel;
    @FXML private Label dateLabel;
    @FXML private Label contenuLabel;
    @FXML private ImageView gifImageView;

    private Commentaire commentaire;
    private Runnable refreshCallback;
    private final CommentaireService commentaireService = new CommentaireService();

    public void setData(Commentaire commentaire, Runnable refreshCallback) {
        this.commentaire = commentaire;
        this.refreshCallback = refreshCallback;

        // Initialiser les données
        auteurLabel.setText(commentaire.getUser().getPrenom() + " " + commentaire.getUser().getNom());
        dateLabel.setText(commentaire.getDateComment());
        contenuLabel.setText(commentaire.getContenu());

        // Gérer l'image GIF si elle existe
        if (commentaire.getGifurl() != null && !commentaire.getGifurl().isEmpty()) {
            try {
                gifImageView.setImage(new Image(commentaire.getGifurl()));
                gifImageView.setVisible(true);
            } catch (Exception e) {
                System.out.println("Erreur de chargement du GIF: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleModifier() {
        TextInputDialog dialog = new TextInputDialog(commentaire.getContenu());
        dialog.setTitle("Modify");
        dialog.setHeaderText("Modify your comment");
        dialog.setContentText("your new Comment");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nouveauContenu -> {
            commentaire.setContenu(nouveauContenu);
            commentaireService.modifier(commentaire);
            if (refreshCallback != null) {
                refreshCallback.run();
            }
        });
    }

    @FXML
    private void handleSupprimer() {
        System.out.println("Tentative de suppression du commentaire ID: " + commentaire.getId());
        System.out.println("Contenu du commentaire: " + commentaire.getContenu());

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer ce commentaire ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer définitivement ce commentaire ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("Utilisateur a confirmé la suppression");

            try {
                commentaireService.supprimer(commentaire.getId());
                System.out.println("Méthode supprimer() appelée avec succès");

                if (refreshCallback != null) {
                    System.out.println("Rafraîchissement de la liste...");
                    refreshCallback.run();
                } else {
                    System.out.println("Attention: refreshCallback est null!");
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la suppression:");
                e.printStackTrace();

                // Afficher une alerte à l'utilisateur
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText("Échec de la suppression");
                errorAlert.setContentText("Une erreur est survenue lors de la suppression du commentaire.");
                errorAlert.showAndWait();
            }
        }
    }
}