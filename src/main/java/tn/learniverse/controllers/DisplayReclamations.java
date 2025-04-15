package tn.learniverse.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import tn.learniverse.entities.User;
import tn.learniverse.entities.Reclamation;
import tn.learniverse.services.ReclamationService;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DisplayReclamations {
    @FXML private VBox reclamationsContainer;
    @FXML private Label headerLabel;
    private final ReclamationService reclamationService = new ReclamationService();

    public void initialize() {
        try {
            User user = new User();
            user.setId(2);
            List<Reclamation> reclamations = reclamationService.recuperer(user);
            if (reclamations.isEmpty()) {
                headerLabel.setText("Aucune réclamation pour l'utilisateur 3");
            } else {
                for (Reclamation rec : reclamations) {
                    reclamationsContainer.getChildren().add(createReclamationBox(rec));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            headerLabel.setText("Erreur lors de la récupération des réclamations");
        }
    }

    private VBox createReclamationBox(Reclamation rec) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; " +
                      "-fx-border-radius: 15px; " +
                      "-fx-background-radius: 15px; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 4); " +
                      "-fx-border-color: #ccc; " +
                      "-fx-border-width: 1; " +
                      "-fx-pref-width: 400;" +
                      "-fx-pref-height: 150;");
        box.getStyleClass().add("card");

        Date dateReclamation = rec.getDateReclamation();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String formattedDate = dateFormat.format(dateReclamation);

        HBox statusDateBox = new HBox(80);
        Label statusLabel = new Label(rec.getStatut());
        statusLabel.getStyleClass().add(getStatusClass(rec.getStatut()));


        Label dateLabel = new Label(formattedDate);
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");

        statusDateBox.getChildren().addAll(statusLabel, dateLabel);

        // Titre de la réclamation
        Label titleLabel = new Label(rec.getTitre());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Text contentText = new Text(rec.getContenu());
        contentText.setWrappingWidth(250);

        HBox actions = new HBox(10);
        if (rec.getStatut().equals("Traité")) {
            Button btnVoirReponse = createButton("Voir Réponse", "btn-primary");
            btnVoirReponse.setOnAction(event -> {
                Node sourceNode = (Node) event.getSource();
                viewResponses(rec, sourceNode);
            });
            Button btnArchiver = createButton("Archiver", "btn-danger");
            actions.getChildren().addAll(btnVoirReponse, btnArchiver);

        } else if (rec.getStatut().equals("En Cours")) {
            actions.getChildren().add(createButton("Voir Réponse", "btn-primary", e -> viewResponses(rec, (Node) e.getSource())));
        } else {
            actions.getChildren().add(createButton("Modifier Contenu", "btn-success"));
        }
        box.getChildren().addAll(statusDateBox, titleLabel, contentText, actions);
        return box;
    }

    private Button createButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().add(styleClass);
        return btn;
    }
    private Button createButton(String text, String styleClass, EventHandler<javafx.event.ActionEvent> eventHandler) {
        Button btn = new Button(text);
        btn.getStyleClass().add(styleClass);
        btn.setOnAction(eventHandler);
        return btn;
    }

    private String getStatusClass(String statut) {
        return switch (statut) {
            case "Traité" -> "statut-traite";
            case "En Cours" -> "statut-encours";
            default -> "statut-non-traite";
        };
    }
    private void viewResponses(Reclamation rec, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reponses.fxml"));
            Parent root = loader.load();
            Reponses responsesController = loader.getController();
            responsesController.setReclamation(rec);

            Stage stage = new Stage();
            stage.setTitle("Réponses à la réclamation");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
