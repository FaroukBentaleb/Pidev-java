package tn.learniverse.controllers.Reclamation;

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
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.learniverse.entities.User;
import tn.learniverse.entities.Reclamation;
import tn.learniverse.services.ReclamationService;
import javafx.event.EventHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReclamationsArchivéesFront {
    private User user;
    public void setUser(User user) {
        this.user = user;
    }
    @FXML
    private VBox reclamationsContainer;
    @FXML
    private Label headerLabel;
    private final ReclamationService reclamationService = new ReclamationService();

    public void initialize() {
        reclamationsContainer.setSpacing(5);

        try {
            User user = new User();
            user.setId(3);
            List<Reclamation> reclamations = reclamationService.recupererReclamationsArchivéesFront(user);
            if (reclamations.isEmpty()) {
                headerLabel.setText("Aucune réclamation pour l'utilisateur 3");
            } else {
                for (Reclamation rec : reclamations) {
                    VBox box = createReclamationBox(rec);
                    VBox.setMargin(box, new Insets(5, 0, 5, 0));
                    reclamationsContainer.getChildren().add(box);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            headerLabel.setText("Erreur lors de la récupération des réclamations");
        }
    }

    private VBox createReclamationBox(Reclamation rec) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(30));
        box.setStyle("-fx-background-color: white; " +
                "-fx-border-radius: 15px; " +
                "-fx-background-radius: 15px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 4); " +
                "-fx-border-color: #ccc; " +
                "-fx-border-width: 1; " +
                "-fx-pref-width: 400;" +
                "-fx-pref-height: 200;" +
                "-fx-padding: 35; " +
                "-fx-margin: 10;");
        box.getStyleClass().add("card");

        Date dateReclamation = rec.getDateReclamation();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String formattedDate = dateFormat.format(dateReclamation);
        Label statusLabel = new Label(rec.getStatut());
        statusLabel.getStyleClass().add(getStatusClass(rec.getStatut()));

        Label dateLabel = new Label(formattedDate);
        dateLabel.setId("date-label");

        HBox statusDateBox = new HBox();
        statusDateBox.getStyleClass().add("status-date-row");
        statusDateBox.getChildren().addAll(statusLabel, dateLabel);

        Label titleLabel = new Label(rec.getTitre());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");
        Text contentText = new Text(rec.getContenu());
        contentText.setWrappingWidth(1000);
        contentText.setStyle("-fx-font-size: 20px;");

        HBox actions = new HBox(10);
        if (rec.getStatut().equals("Traité")) {
            Button btnVoirReponse = createButton("Voir Réponse", "btn-primary");
            btnVoirReponse.setOnAction(event -> {
                Node sourceNode = (Node) event.getSource();
                viewResponses(rec, sourceNode);
            });
            actions.getChildren().add(btnVoirReponse);
        } else if (rec.getStatut().equals("En Cours")) {
            actions.getChildren().add(createButton("Voir Réponse", "btn-primary",
                    e -> viewResponses(rec, (Node) e.getSource())));
        } else {
            actions.getChildren().add(createButton("Modifier Contenu", "btn-success",
                    e -> openModifierReclamationDialog(rec)));
        }

        box.getChildren().addAll(statusDateBox, titleLabel, contentText, actions);
        return box;
    }

    private Button createButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().add(styleClass);
        return btn;
    }

    private Button createButton(String text, String styleClass, EventHandler<ActionEvent> eventHandler) {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/Reponses.fxml"));
            Parent responsesRoot = loader.load();
            Reponses responsesController = loader.getController();
            responsesController.setReclamation(rec);

            // Clear the current content and add the responses view
            reclamationsContainer.getChildren().clear();
            reclamationsContainer.getChildren().add(responsesRoot);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    private void openModifierReclamationDialog(Reclamation reclamation) {
        try {
            VBox modifierBox = new VBox(10);
            modifierBox.setPadding(new Insets(20));
            modifierBox.setStyle("-fx-background-color: #f0f4f8; -fx-border-radius: 15px; -fx-background-radius: 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 4);");
            TextArea reclamationContenu = new TextArea(reclamation.getContenu());
            reclamationContenu.setPrefHeight(250);
            reclamationContenu.setPrefWidth(540);
            reclamationContenu.setPromptText("Décrivez votre réclamation en détail...");
            reclamationContenu.setStyle("-fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-font-size: 14;");
            Button btnModifier = new Button("Modifier");
            btnModifier.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            btnModifier.setOnAction(event -> {
                String nouveauContenu = reclamationContenu.getText();
                try {
                    reclamationService.modifier(reclamation.getId(), nouveauContenu);
                    // Fermer la fenêtre après modification
                    Stage stage = (Stage) btnModifier.getScene().getWindow();
                    stage.close();
                    // Retourner à l'affichage des réclamations
                    initialize();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            Button btnAnnuler = new Button("Annuler");
            btnAnnuler.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            btnAnnuler.setOnAction(event -> {
                Stage stage = (Stage) btnAnnuler.getScene().getWindow();
                stage.close();
            });

            HBox actions = new HBox(10, btnModifier, btnAnnuler);
            actions.setPadding(new Insets(10));
            actions.setStyle("-fx-alignment: center;");

            modifierBox.getChildren().addAll(reclamationContenu, actions);

            // Créer une nouvelle scène et un nouveau stage pour afficher l'interface de modification
            Scene scene = new Scene(modifierBox);
            Stage stage = new Stage();
            stage.setTitle("Modifier la réclamation");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void afficherReclamationsArchivees(VBox container) {
        try {
            // Récupérer les réclamations archivées
            List<Reclamation> reclamationsArchivees = reclamationService.recupererReclamationsArchivéesFront(user);
            container.getChildren().clear();

            if (reclamationsArchivees.isEmpty()) {
                headerLabel.setText("Aucune réclamation archivée pour l'utilisateur " + user.getId());
            } else {
                for (Reclamation rec : reclamationsArchivees) {
                    VBox box = createReclamationBox(rec);
                    VBox.setMargin(box, new Insets(5, 0, 5, 0));
                    container.getChildren().add(box);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            headerLabel.setText("Erreur lors de la récupération des réclamations archivées");
        }
    }

    public void retourListeReclamations() {
        Stage stage = (Stage) reclamationsContainer.getScene().getWindow();
        stage.close();
    }
}

