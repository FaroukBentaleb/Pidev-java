package tn.learniverse.controllers.Reclamation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.learniverse.entities.Reclamation;
import tn.learniverse.services.ReclamationService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import tn.learniverse.tools.Navigator;

public class ReclamationsArchivéesBack {
    @FXML
    private VBox reclamationsContainer;
    @FXML
    private Label headerLabel;
    private final ReclamationService reclamationService = new ReclamationService();

    public void initialize() {
        reclamationsContainer.setSpacing(5);
        try {
            List<Reclamation> reclamations = reclamationService.recupererReclamationsArchivéesBack();
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

        box.getChildren().addAll(statusDateBox, titleLabel, contentText);
        if (rec.getFichier() != null && !rec.getFichier().isEmpty()) {
            HBox fileBox = new HBox(8);
            fileBox.setStyle("-fx-background-color: #f6f6f6; -fx-padding: 8 12; -fx-background-radius: 8;");
            ImageView fileIcon = new ImageView();
            fileIcon.setFitHeight(32);
            fileIcon.setFitWidth(32);

            String fileName = new java.io.File(rec.getFichier()).getName();
            Label fileLabel = new Label(fileName);
            fileLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

            if (fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                fileIcon.setImage(new Image("file:///" + rec.getFichier().replace("\\", "/")));
            } else if (fileName.toLowerCase().endsWith(".pdf")) {
                fileIcon.setImage(new Image("file:///C:/wamp64/www/images/icons/pdf_icon.png"));
            }

            fileBox.getChildren().addAll(fileIcon, fileLabel);
            fileBox.setOnMouseClicked(e -> {
                try {
                    java.io.File file = new java.io.File(rec.getFichier());
                    if (!file.exists()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Fichier introuvable");
                        alert.setHeaderText(null);
                        alert.setContentText("Le fichier " + file.getName() + " n'existe pas ou a été supprimé.");
                        alert.showAndWait();
                        return;
                    }
                    java.awt.Desktop.getDesktop().open(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur d'ouverture");
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible d'ouvrir le fichier.");
                    alert.showAndWait();
                }
            });
            box.getChildren().add(fileBox);
        }
        box.getChildren().add(actions);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/ReponsesBack.fxml"));
            Parent root = loader.load();

            ReponsesBack responsesController = loader.getController();
            responsesController.setReclamation(rec);
            Scene currentScene = sourceNode.getScene();
            currentScene.setRoot(root);

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Une erreur est survenue lors de l'affichage des réponses : " + e.getMessage());
            errorAlert.showAndWait();
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
                    Stage stage = (Stage) btnModifier.getScene().getWindow();
                    stage.close();
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
            List<Reclamation> reclamationsArchivees = reclamationService.recupererReclamationsArchivéesBack();
            container.getChildren().clear();

            if (reclamationsArchivees.isEmpty()) {
                headerLabel.setText("Aucune réclamation archivée ");
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

    public void retourListeReclamations(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Reclamation/DisplayReclamationBack.fxml");
    }

    public void Dashboard(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Dashboard.fxml");
    }

    public void complaints(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Reclamation/DisplayReclamationBack.fxml");
    }
}
