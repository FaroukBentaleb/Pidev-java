package tn.learniverse.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import java.util.stream.Collectors;
import javafx.stage.Stage;
import tn.learniverse.entities.Like;
import tn.learniverse.entities.Poste;
import tn.learniverse.entities.User;
import tn.learniverse.services.LikeService;
import tn.learniverse.services.PosteService;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tn.learniverse.tools.Session;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public class PosteItemController {

    public Button CommentBtn;
    private Runnable onDeleteCallback;
    private Poste poste;

    @FXML
    private Label auteurLabel;

    @FXML
    private HBox emojiBox;

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

    private LikeService likeService = new LikeService(); // Service pour gérer les Likes
    private User connectedUser; // Utilisateur connecté

    // On initialise l'utilisateur connecté ici
    public void setData(Poste poste, Runnable refreshCallback) {
        this.poste = poste;
        this.onDeleteCallback = refreshCallback;

        // Simuler l'utilisateur connecté
        connectedUser = new User();
        if(Session.getCurrentUser().getEmail()!=null) {
            connectedUser.setId(Session.getCurrentUser().getId());
        }
        else{
            connectedUser.setId(1);
        }

        // Remplir les autres champs comme tu le fais déjà
        auteurLabel.setText(poste.getUser().getPrenom() + " " + poste.getUser().getNom());
        categorieLabel.setText(poste.getCategorie());
        dateLabel.setText(poste.getDatePost());
        nbCommentairesLabel.setText(poste.getNbCom() + " comments");
        titreLabel.setText(poste.getTitre());
        contenuLabel.setText(poste.getContenu());

        if (poste.getPhoto() != null && !poste.getPhoto().isEmpty()) {
            try {
                File imageFile = new File("C:/wamp64/www/" + poste.getPhoto());
                if (imageFile.exists()) {
                    String photoPath = imageFile.toURI().toString();
                    Image image = new Image(photoPath, true);
                    photoImageView.setImage(image);
                    photoImageView.setVisible(true);
                    photoImageView.setManaged(true); // affiche l'espace si photo existe
                } else {
                    photoImageView.setVisible(false);
                    photoImageView.setManaged(false); // enlève l'espace si pas de photo
                }
            } catch (Exception e) {
                photoImageView.setVisible(false);
                photoImageView.setManaged(false);
            }
        } else {
            photoImageView.setVisible(false);
            photoImageView.setManaged(false);
        }
        if(poste.getUser().getId()!=Session.getCurrentUser().getId()) {
            updateB.setVisible(false);
            deleteB.setVisible(false);
        }


        setupEmojis(); // Configurer les emojis
    }

    private void setupEmojis() {
        emojiBox.getChildren().clear(); // Vider les emojis existants

        String[] emojis = {"😃", "❤", "😡"};
        String[] types = {"smile", "love", "angry"};

        try {
            // Compter les likes par type
            Map<String, Integer> compteur = likeService.compterLikesParType(poste);

            for (int i = 0; i < emojis.length; i++) {
                String emoji = emojis[i];
                String type = types[i];

                // Compter combien de fois chaque type de like a été cliqué
                int count = compteur.getOrDefault(type, 0);

                Button emojiButton = new Button(emoji + " " + count);
                emojiButton.getStyleClass().addAll("emoji-button", "emoji-" + type);


                // IMPORTANT : Déclarer type et emojiButton comme final pour pouvoir les utiliser dans le setOnAction
                final String finalType = type;

                // Ajouter un listener d'action pour chaque bouton emoji
                emojiButton.setOnAction(event -> {
                    try {
                        if (!likeService.existeDeja(poste, connectedUser, finalType)) {
                            // Si pas encore liké → ajouter
                            Like like = new Like(finalType, poste, connectedUser);
                            likeService.ajouter(like);
                            System.out.println("Like ajouté pour " + finalType);
                        } else {
                            // Si déjà liké → supprimer
                            Like like = likeService.getLike(poste.getId(), connectedUser.getId(), finalType);

                            if (like != null) {
                                likeService.supprimer(like.getId());
                                System.out.println("Like supprimé pour " + finalType);
                            }
                        }
                        // Après ajout ou suppression → rafraîchir les compteurs
                        setupEmojis();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                // Ajouter le bouton dans le HBox des emojis
                emojiBox.getChildren().add(emojiButton);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Supprimer un post
    @FXML
    private void handleDelete(ActionEvent event) {
        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete");
            confirm.setContentText("Warning: This will permanently delete the selected item :" + poste.getTitre()+ ". Are you absolutely sure?" );

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                new PosteService().supprimer(poste.getId());


                if (onDeleteCallback != null) {
                    onDeleteCallback.run(); // Rafraîchir la liste des posts
                }
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error : " + e.getMessage()).show();
        }
    }

    // Modifier un post
    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierPoste.fxml"));
            Parent root = loader.load();

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

            alert.setContentText("Error " + e.getMessage());
            alert.showAndWait();
        }
    }

    // Ouvrir la fenêtre des commentaires
    @FXML
    public void ouvrirCommentaires(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherCommentaires.fxml"));
            Parent root = loader.load();

            AfficherCommentairesController controller = loader.getController();
            controller.setPoste(poste, onDeleteCallback);

            Stage stage = new Stage();
            stage.setTitle("Commentaires - " + poste.getTitre());
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Error while opening comments");
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
    public Button getCommentBtn() {
        return CommentBtn;
    }

    @FXML
    private Button traduireButton;

    private boolean traduit = false;
    private String originalContent = null;


    private String translateText(String text, String targetLang) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return "Aucun texte à traduire";
        }

        // MyMemory API : https://api.mymemory.translated.net/get?q=bonjour&langpair=fr|en
        String sourceLang = "en"; // ou "en", "fr", etc.

        String urlStr = String.format("https://api.mymemory.translated.net/get?q=%s&langpair=%s|%s",
                URLEncoder.encode(text, "UTF-8"), sourceLang, targetLang);

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String response = reader.lines().collect(Collectors.joining());
            JsonNode node = new ObjectMapper().readTree(response);
            return node.path("responseData").path("translatedText").asText();
        } catch (Exception e) {
            throw new IOException("Erreur pendant la traduction : " + e.getMessage());
        }
    }


    @FXML
    private void handleTraduire(ActionEvent event) {
        if (!traduit) {
            originalContent = contenuLabel.getText(); // Sauvegarder le texte original
            traduireButton.setText(".."); // Afficher une sorte de chargement dans le bouton
            traduireButton.setDisable(true); // Désactiver le bouton pendant la traduction

            // Lancer la traduction dans un thread séparé pour éviter de bloquer l'UI
            new Thread(() -> {
                try {
                    String translated = translateText(originalContent, "fr"); // Traduction vers le français

                    // Mettre à jour l'UI sur le thread principal après la traduction
                    Platform.runLater(() -> {
                        contenuLabel.setText(translated); // Remplacer le contenu par la traduction
                        traduireButton.setText("🌍"); // Changer l'icône du bouton
                        traduireButton.setDisable(false); // Réactiver le bouton
                        traduit = true; // Indiquer que le texte est traduit
                    });
                } catch (IOException e) {
                    // Si erreur, afficher un message d'erreur
                    Platform.runLater(() -> {
                        contenuLabel.setText("Erreur : " + e.getMessage());
                        traduireButton.setText("Traduire");
                        traduireButton.setDisable(false);
                    });
                }
            }).start();
        } else {
            // Si déjà traduit, revenir au texte original
            contenuLabel.setText(originalContent);
            traduireButton.setText("🌍"); // Réinitialiser le texte du bouton
            traduit = false; // Indiquer que le texte est original
        }
    }






}
