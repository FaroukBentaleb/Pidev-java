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
import tn.learniverse.entities.Reponse;
import tn.learniverse.entities.User;
import tn.learniverse.entities.Reclamation;
import tn.learniverse.services.ReclamationService;
import javafx.event.EventHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javafx.geometry.Pos;
import tn.learniverse.services.ReponseService;

public class DisplayReclamationBack {
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
        reclamationsContainer.getChildren().clear();
        reclamationsContainer.setSpacing(5);

        try {
            User user = new User();
            user.setId(2);
            List<Reclamation> reclamations = reclamationService.recupererReclamationsBack();
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
        contentText.setWrappingWidth(600);
        contentText.setStyle("-fx-font-size: 20px;");

        HBox actions = new HBox(10);
        if (rec.getStatut().equals("Traité")) {
            Button btnVoirReponse = createButton("Voir Réponse", "btn-primary");
            btnVoirReponse.setOnAction(event -> {
                Node sourceNode = (Node) event.getSource();
                viewResponses(rec, sourceNode);
            });
            Button btnArchiver = createButton("Archiver", "btn-danger");
            btnArchiver.setOnAction(event -> archiverReclamation(rec));
            actions.getChildren().addAll(btnVoirReponse, btnArchiver);
        } else if (rec.getStatut().equals("En Cours")) {
            actions.getChildren().add(createButton("Voir Réponse", "btn-primary",
                    e -> viewResponses(rec, (Node) e.getSource())));
        } else {
            actions.getChildren().add(createButton("Répondre", "btn-success",
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/ReponsesBack.fxml"));
            Parent root = loader.load();
            
            // Récupérer le contrôleur et initialiser la réclamation
            ReponsesBack responsesController = loader.getController();
            responsesController.setReclamation(rec);
            
            // Obtenir la scène actuelle
            Scene currentScene = sourceNode.getScene();
            
            // Mettre à jour la scène avec la nouvelle vue
            currentScene.setRoot(root);
            
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            // Afficher une alerte en cas d'erreur
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Une erreur est survenue lors de l'affichage des réponses : " + e.getMessage());
            errorAlert.showAndWait();
        }
    }
    private void openModifierReclamationDialog(Reclamation reclamation) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Répondre à la réclamation");

            VBox mainContainer = new VBox(20);
            mainContainer.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 30;");
            mainContainer.setPrefWidth(600);
            mainContainer.setAlignment(Pos.CENTER);

            // Titre de la réclamation
            Label titleLabel = new Label("Répondre à : " + reclamation.getTitre());
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            // Zone de texte pour la réponse avec style moderne
            TextArea reponseTextArea = new TextArea();
            reponseTextArea.setPromptText("Votre réponse ici (minimum 10 caractères)");
            reponseTextArea.setPrefRowCount(10);
            reponseTextArea.setWrapText(true);
            reponseTextArea.setStyle("-fx-font-size: 14px; -fx-background-color: white; " +
                    "-fx-border-color: #e0e0e0; -fx-border-radius: 5px; " +
                    "-fx-background-radius: 5px; -fx-padding: 10px;");

            // Label pour les messages d'erreur
            Label errorLabel = new Label();
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
            errorLabel.setVisible(false);

            // Conteneur pour les boutons
            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER);

            Button btnRepondre = new Button("Répondre");
            btnRepondre.setDisable(true); // Désactivé par défaut
            btnRepondre.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");

            Button btnAnnuler = new Button("Annuler");
            btnAnnuler.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");

            buttonBox.getChildren().addAll(btnRepondre, btnAnnuler);

            // Contrôle de saisie avec listener
            reponseTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.trim().length() < 10) {
                    errorLabel.setText("La réponse doit contenir au moins 10 caractères");
                    errorLabel.setVisible(true);
                    btnRepondre.setDisable(true);
                } else {
                    errorLabel.setVisible(false);
                    btnRepondre.setDisable(false);
                }
            });

            // Action du bouton Répondre
            btnRepondre.setOnAction(event -> {
                try {
                    // Créer et sauvegarder la réponse
                    User admin = new User();
                    admin.setId(2); // ID de l'admin
                    admin.setRole("Admin");
                    
                    Reponse reponse = new Reponse(reponseTextArea.getText(), new Date(), reclamation, admin, 0);
                    ReponseService reponseService = new ReponseService();
                    reponseService.ajouter(reponse, admin, reclamation);

                    reclamationService.updateStatut(reclamation.getId(), "En Cours");
                    stage.close();

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/ReponsesBack.fxml"));
                    Parent root = loader.load();
                    ReponsesBack controller = loader.getController();
                    List<Reclamation> reclamations = reclamationService.recupererReclamationsBack();
                    Reclamation updatedReclamation = reclamations.stream()
                            .filter(r -> r.getId() == reclamation.getId())
                            .findFirst()
                            .orElse(reclamation);
                    
                    controller.setReclamation(updatedReclamation);
                    
                    // Mettre à jour la scène
                    Scene scene = btnRepondre.getScene();
                    Stage primaryStage = (Stage) scene.getWindow();
                    primaryStage.setScene(new Scene(root));

                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Une erreur est survenue lors de l'ajout de la réponse : " + e.getMessage());
                    errorAlert.showAndWait();
                }
            });

            // Action du bouton Annuler
            btnAnnuler.setOnAction(event -> stage.close());

            // Ajouter tous les éléments au conteneur principal
            mainContainer.getChildren().addAll(
                titleLabel,
                reponseTextArea,
                errorLabel,
                buttonBox
            );

            // Créer et afficher la scène
            Scene scene = new Scene(mainContainer);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Une erreur est survenue : " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    @FXML
    private void afficherReclamationsArchivees() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/ReclamationsArchivéesFront.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Réclamations Archivées");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void afficherAjouterReclamation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/AjouterReclamation.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter Réclamation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            // Rafraîchir l'interface après la fermeture de la fenêtre d'ajout
            initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void archiverReclamation(Reclamation reclamation) {
        try {
            // 1. Archiver la réclamation
            reclamationService.ArchiverBack(reclamation.getId(), 1);
            
            // 2. Rafraîchir l'affichage immédiatement
            refreshDisplay();
            
            // 3. Afficher un message de succès
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Succès");
            successAlert.setHeaderText(null);
            successAlert.setContentText("La réclamation a été archivée avec succès !");
            successAlert.showAndWait();
            
        } catch (SQLException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Erreur lors de l'archivage : " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    // Nouvelle méthode pour rafraîchir l'affichage
    private void refreshDisplay() {
        try {
            // Vider le conteneur
            reclamationsContainer.getChildren().clear();
            
            // Récupérer les réclamations non archivées
            List<Reclamation> reclamations = reclamationService.recupererReclamationsBack();
            
            // Mettre à jour l'affichage
            if (reclamations.isEmpty()) {
                headerLabel.setText("Aucune réclamation non archivée");
            } else {
                for (Reclamation rec : reclamations) {
                    VBox box = createReclamationBox(rec);
                    VBox.setMargin(box, new Insets(5, 0, 5, 0));
                    reclamationsContainer.getChildren().add(box);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Erreur lors du rafraîchissement : " + e.getMessage());
            errorAlert.showAndWait();
        }
    }
}
