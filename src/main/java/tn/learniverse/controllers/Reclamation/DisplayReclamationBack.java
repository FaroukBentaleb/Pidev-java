package tn.learniverse.controllers.Reclamation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
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
import tn.learniverse.services.FlaskClient;
import javafx.event.EventHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javafx.geometry.Pos;
import tn.learniverse.services.ReponseService;
import tn.learniverse.services.EmailService;
import javax.mail.MessagingException;

public class DisplayReclamationBack {
    private User user;
    public void setUser(User user) {
        this.user = user;
    }
    @FXML
    private VBox reclamationsContainer;
    @FXML
    private Label headerLabel;
    @FXML
    private TextField searchField;
    @FXML
    private Pagination pagination;
    private final ReclamationService reclamationService = new ReclamationService();
    private final int ITEMS_PER_PAGE = 5;

    public void initialize() {
        try {
            reclamationsContainer.setSpacing(5);

            int totalPages = reclamationService.getTotalPages(ITEMS_PER_PAGE);

            pagination.setPageCount(Math.max(1, totalPages));
            pagination.setCurrentPageIndex(0);
            pagination.setMaxPageIndicatorCount(100);

            pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
                try {
                    loadReclamationsForPage(newIndex.intValue() + 1);
                } catch (SQLException e) {
                    e.printStackTrace();
                    showErrorMessage("Erreur lors du chargement des réclamations");
                }
            });

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (newValue == null || newValue.trim().isEmpty()) {
                        pagination.setVisible(true);
                        pagination.setManaged(true);
                        loadReclamationsForPage(1);
                    } else {
                        pagination.setVisible(false);
                        pagination.setManaged(false);
                        List<Reclamation> reclamations = reclamationService.rechercherBack(newValue.trim());
                        displayReclamations(reclamations);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showErrorMessage("Erreur lors de la recherche des réclamations");
                }
            });

            loadReclamationsForPage(1);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Erreur lors de l'initialisation");
        }
    }

    private void loadReclamationsForPage(int page) throws SQLException {
        List<Reclamation> reclamations = reclamationService.recupererReclamationsBackPaginees(page, ITEMS_PER_PAGE);
        displayReclamations(reclamations);
    }

    private void displayReclamations(List<Reclamation> reclamations) {
        reclamationsContainer.getChildren().clear();
        
        if (reclamations.isEmpty()) {
            VBox emptyMessageBox = new VBox();
            emptyMessageBox.setAlignment(Pos.CENTER);
            emptyMessageBox.setPrefHeight(400);
            
            Label emptyMessage = new Label("Aucune réclamation disponible");
            emptyMessage.setStyle("-fx-font-size: 24px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-text-fill: #666666; " +
                                "-fx-background-color: white; " +
                                "-fx-padding: 20px 40px; " +
                                "-fx-background-radius: 10px; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 4);");
            
            emptyMessageBox.getChildren().add(emptyMessage);
            reclamationsContainer.getChildren().add(emptyMessageBox);
            reclamationsContainer.setAlignment(Pos.CENTER);
        } else {
            for (Reclamation rec : reclamations) {
                VBox box = createReclamationBox(rec);
                VBox.setMargin(box, new Insets(5, 0, 5, 0));
                reclamationsContainer.getChildren().add(box);
            }
        }
    }

    private void showErrorMessage(String message) {
        if (headerLabel != null) {
            headerLabel.setText(message);
        } else {
            System.err.println(message);
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
            Stage stage = new Stage();
            stage.setTitle("Répondre à la réclamation");

            VBox mainContainer = new VBox(20);
            mainContainer.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 30;");
            mainContainer.setPrefWidth(1100);
            mainContainer.setPrefHeight(550);
            mainContainer.setAlignment(Pos.CENTER);

            Label titleLabel = new Label("Répondre à : " + reclamation.getTitre());
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            Label descriptionLabel = new Label("Contenu : " + reclamation.getContenu());
            descriptionLabel.setWrapText(true);
            descriptionLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #34495e;");

            TextArea reponseTextArea = new TextArea();
            reponseTextArea.setPromptText("Votre réponse ici (minimum 10 caractères)");
            reponseTextArea.setPrefRowCount(10);
            reponseTextArea.setWrapText(true);
            reponseTextArea.setStyle("-fx-font-size: 14px; -fx-background-color: white; " +
                    "-fx-border-color: #e0e0e0; -fx-border-radius: 5px; " +
                    "-fx-background-radius: 5px; -fx-padding: 10px;");

            Label suggestionLabel = new Label();
            suggestionLabel.setWrapText(true);
            suggestionLabel.setCursor(Cursor.HAND);
            suggestionLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; " +
                    "-fx-text-fill: #3498db; -fx-background-color: white; -fx-padding: 5 10; " +
                    "-fx-border-color: #3498db; -fx-border-radius: 15px; -fx-background-radius: 15px;");

            suggestionLabel.setOnMouseClicked(event -> {
                String suggestionText = suggestionLabel.getText().replace("Suggestion : ", "").trim();
                if (!suggestionText.isEmpty()) {
                    reponseTextArea.setText(suggestionText);
                }
            });

            Label errorLabel = new Label();
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
            errorLabel.setVisible(false);

            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER);

            Button btnRepondre = new Button("Répondre");
            btnRepondre.setDisable(true);
            btnRepondre.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");

            Button btnAnnuler = new Button("Annuler");
            btnAnnuler.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");

            buttonBox.getChildren().addAll(btnRepondre, btnAnnuler);

            reponseTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.trim().length() < 10) {
                    errorLabel.setText("La réponse doit contenir au moins 10 caractères");
                    errorLabel.setVisible(true);
                    btnRepondre.setDisable(true);
                } else {
                    errorLabel.setVisible(false);
                    btnRepondre.setDisable(false);
                }

                if (!newValue.trim().isEmpty()) {
                    new Thread(() -> {
                        String suggestion = FlaskClient.getSuggestion(reclamation.getContenu(), newValue);
                        Platform.runLater(() -> suggestionLabel.setText("Suggestion : " + suggestion));
                    }).start();
                } else {
                    suggestionLabel.setText("");
                }
            });

            btnRepondre.setOnAction(event -> {
                try {
                    User admin = new User();
                    admin.setId(2);
                    admin.setRole("Admin");

                    Reponse reponse = new Reponse(reponseTextArea.getText(), new Date(), reclamation, admin, 0);
                    ReponseService reponseService = new ReponseService();
                    reponseService.ajouter(reponse, admin, reclamation);

                    reclamationService.updateStatut(reclamation.getId(), "En Cours");

                    String emailContent = String.format("""
                        <h3>Détails de la réclamation :</h3>
                        <p><strong>Titre :</strong> %s</p>
                        <p><strong>Date de réclamation :</strong> %s</p>
                        <p><strong>Contenu :</strong> %s</p>
                        <h3>Réponse à la Reclamation :</h3>
                        <p><strong>Date de réponse :</strong> %s</p>
                        <p>%s</p>
                        """, 
                        reclamation.getTitre(),
                        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(reclamation.getDateReclamation()),
                        reclamation.getContenu(),
                        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()),
                        reponseTextArea.getText()
                    );

                    try {
                        EmailService.sendEmail(
                            reclamation.getUser().getEmail(),
                            "Réponse à votre réclamation - " + reclamation.getTitre(),
                            emailContent
                        );
                    } catch (MessagingException e) {
                        System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
                    }

                    stage.close();
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Succès");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Réponse ajoutée avec succès !");
                    successAlert.showAndWait();
                    try {
                        int currentPage = pagination.getCurrentPageIndex() + 1;
                        loadReclamationsForPage(currentPage);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showErrorMessage("Erreur lors du rafraîchissement de l'interface");
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Une erreur est survenue lors de l'ajout de la réponse : " + e.getMessage());
                    errorAlert.showAndWait();
                }
            });

            btnAnnuler.setOnAction(event -> stage.close());

            mainContainer.getChildren().addAll(
                    titleLabel,
                    descriptionLabel,
                    reponseTextArea,
                    suggestionLabel,
                    errorLabel,
                    buttonBox
            );

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
            initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void archiverReclamation(Reclamation reclamation) {
        try {
            reclamationService.ArchiverBack(reclamation.getId(), 1);
            refreshDisplay();
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
    private void refreshDisplay() {
        try {
            reclamationsContainer.getChildren().clear();

            List<Reclamation> reclamations = reclamationService.recupererReclamationsBack();

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
