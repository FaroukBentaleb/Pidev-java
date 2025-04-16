package tn.learniverse.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import tn.learniverse.entities.Commentaire;
import tn.learniverse.entities.Poste;
import tn.learniverse.services.CommentaireService;

import java.io.IOException;
import java.util.List;

public class AfficherCommentairesController {

    @FXML private VBox commentairesVBox;
    @FXML private TextField commentaireField;
    @FXML private VBox posteContainer;

    private Poste poste;
    private Runnable refreshCallback;
    private final CommentaireService commentaireService = new CommentaireService();

    public void setPoste(Poste poste, Runnable refreshCallback) {
        this.poste = poste;
        this.refreshCallback = refreshCallback;
        chargerPoste();
        chargerCommentaires();
    }

    private void chargerPoste() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PosteItem.fxml"));
            Parent posteNode = loader.load();

            PosteItemController controller = loader.getController();
            controller.setData(poste, refreshCallback);

            controller.getDeleteB().setVisible(false);
            controller.getUpdateB().setVisible(false);

            posteContainer.getChildren().add(posteNode);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le poste", Alert.AlertType.ERROR);
        }
    }

    private void chargerCommentaires() {
        commentairesVBox.getChildren().clear();
        List<Commentaire> commentaires = commentaireService.getByPosteId(poste.getId());

        if (commentaires.isEmpty()) {
            Label aucunCommentaire = new Label("Aucun commentaire pour ce poste");
            aucunCommentaire.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
            commentairesVBox.getChildren().add(aucunCommentaire);
        } else {
            for (Commentaire c : commentaires) {
                commentairesVBox.getChildren().add(creerCommentaireNode(c));
            }
        }
    }

    private Node creerCommentaireNode(Commentaire commentaire) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CommentaireItem.fxml"));
            Parent node = loader.load();

            CommentaireItemController controller = loader.getController();

            // Passer la référence à la méthode de rafraîchissement
            controller.setData(commentaire, this::chargerCommentaires);

            return node;
        } catch (IOException e) {
            e.printStackTrace();
            return new Label("Erreur de chargement du commentaire");
        }
    }

    @FXML
    private void ajouterCommentaire() {
        String contenu = commentaireField.getText().trim();
        if (contenu.isEmpty()) {
            showAlert("Erreur", "Le commentaire ne peut pas être vide", Alert.AlertType.WARNING);
            return;
        }

        try {
            Commentaire nouveauCommentaire = new Commentaire();
            nouveauCommentaire.setContenu(contenu);
            nouveauCommentaire.setPoste(poste);
            // À adapter selon votre modèle utilisateur
            // nouveauCommentaire.setUser(currentUser);

            commentaireService.ajouter(nouveauCommentaire);

            commentaireField.clear();
            chargerCommentaires();

            if (refreshCallback != null) {
                refreshCallback.run();
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ajouter le commentaire: " + e.getMessage(), Alert.AlertType.ERROR);
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