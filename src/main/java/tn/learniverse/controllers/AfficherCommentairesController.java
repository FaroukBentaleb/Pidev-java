package tn.learniverse.controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import tn.learniverse.entities.Commentaire;
import tn.learniverse.entities.Poste;
import tn.learniverse.services.CommentaireService;
import tn.learniverse.tools.Session;

import java.io.IOException;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class AfficherCommentairesController {

    @FXML private VBox commentairesVBox;
    @FXML private TextField commentaireField;
    @FXML private VBox posteContainer;

    @FXML
    private Button gifButton;

    private String selectedGifUrl;
    @FXML
    private ImageView gifPreview;





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
            controller.getCommentBtn().setVisible(false);

            posteContainer.getChildren().add(posteNode);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le poste", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void ouvrirFenetreGif(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RechercheGif.fxml"));
            Parent root = loader.load();

            RechercheGifController controller = loader.getController();
            controller.setGifSelectionListener(selectedUrl -> {
                this.selectedGifUrl = selectedUrl;
                gifPreview.setImage(new Image(selectedUrl)); // Affiche le GIF
            });


            Stage stage = new Stage();
            stage.setTitle("Choisir un GIF");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void chargerCommentaires() {
        commentairesVBox.getChildren().clear();
        List<Commentaire> commentaires = commentaireService.getByPosteId(poste.getId());

        if (commentaires.isEmpty()) {
            Label aucunCommentaire = new Label("No comments for this post");
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

    /*@FXML
    private void ajouterCommentaire() {
        String contenu = commentaireField.getText().trim();
        if (contenu.isEmpty()) {
            showAlert("Erreur", "Le commentaire ne peut pas être vide", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Filtrer le contenu avant de l'enregistrer
            String contenuFiltré = BadWordsFilter.filtrerTexte(contenu);

            Commentaire nouveauCommentaire = new Commentaire();
            nouveauCommentaire.setContenu(contenuFiltré);
            nouveauCommentaire.setGifurl(selectedGifUrl);
            nouveauCommentaire.setPoste(poste);
            // nouveauCommentaire.setUser(currentUser); // à activer si nécessaire

            commentaireService.ajouter(nouveauCommentaire);

            commentaireField.clear();
            gifPreview.setImage(null);
            selectedGifUrl = null;
            chargerCommentaires();

            if (refreshCallback != null) {
                refreshCallback.run();
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ajouter le commentaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }*/

    @FXML
    private void ajouterCommentaire() {
        String contenu = commentaireField.getText().trim();
        if (contenu.isEmpty()) {
            showAlert("Erreur", "Le commentaire ne peut pas être vide", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Étape 1 : Filtrage du texte
            String contenuFiltre = filtrerContenu(contenu);

            Commentaire nouveauCommentaire = new Commentaire();
            nouveauCommentaire.setContenu(contenuFiltre);
            nouveauCommentaire.setGifurl(selectedGifUrl);
            nouveauCommentaire.setPoste(poste);
            nouveauCommentaire.setUser(Session.getCurrentUser());
            // nouveauCommentaire.setUser(currentUser); // à activer si besoin

            commentaireService.ajouter(nouveauCommentaire);

            commentaireField.clear();
            gifPreview.setImage(null);
            selectedGifUrl = null;
            chargerCommentaires();

            if (refreshCallback != null) {
                refreshCallback.run();
            }

        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ajouter le commentaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private String filtrerContenu(String texte) throws Exception {
        String texteEncode = URLEncoder.encode(texte, "UTF-8");

        // Tu peux ajouter des mots personnalisés avec "&add="
        String urlStr = "https://www.purgomalum.com/service/plain?text=" + texteEncode + "&add=chien,merde,salope,con,pute";

        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}