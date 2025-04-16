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
        // Boîte de dialogue pour modifier le commentaire
        TextInputDialog dialog = new TextInputDialog(commentaire.getContenu());
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText("Modification du commentaire");
        dialog.setContentText("Nouveau texte:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nouveauContenu -> {
            commentaire.setContenu(nouveauContenu);
            commentaireService.modifier(commentaire);
            if (refreshCallback != null) {
                refreshCallback.run(); // Rafraîchir la liste
            }
        });
    }

    @FXML
    private void handleSupprimer() {
        // Confirmation avant suppression
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer ce commentaire ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer définitivement ce commentaire ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            commentaireService.supprimer(commentaire.getId());
            if (refreshCallback != null) {
                refreshCallback.run(); // Rafraîchir la liste
            }
        }
    }
}