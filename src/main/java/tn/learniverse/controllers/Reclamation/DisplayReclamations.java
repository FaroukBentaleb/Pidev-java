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
import tn.learniverse.services.ChatBotReclamationService;

public class DisplayReclamations {
    private User user;
    private final ChatBotReclamationService chatGPT = new ChatBotReclamationService();
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
    private TextArea chatArea;
    @FXML
    private TextField inputField;
    private final ReclamationService reclamationService = new ReclamationService();
    @FXML
    private Button chatbotButton;
    @FXML
    private Label hoverMessage;
    @FXML
    private VBox chatContainer;

    public void initialize() {
        reclamationsContainer.getChildren().clear();
        reclamationsContainer.setSpacing(5);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (user == null) {
                    user = new User();
                    user.setId(3);
                    user.setRole("Student");
                }
                
                reclamationsContainer.getChildren().clear();
                List<Reclamation> reclamations;
                
                if (newValue == null || newValue.trim().isEmpty()) {
                    reclamations = reclamationService.recuperer(user);
                } else {
                    reclamations = reclamationService.rechercher(newValue.trim(), user);
                }

                if (reclamations.isEmpty()) {
                    VBox emptyMessageBox = new VBox();
                    emptyMessageBox.setAlignment(javafx.geometry.Pos.CENTER);
                    emptyMessageBox.setPrefHeight(400);
                    Label emptyMessage = new Label(newValue.trim().isEmpty() ? 
                        "Aucune réclamation disponible" : 
                        "Aucune réclamation trouvée pour : " + newValue);
                    emptyMessage.setStyle("-fx-font-size: 24px; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-text-fill: #666666; " +
                                        "-fx-background-color: white; " +
                                        "-fx-padding: 20px 40px; " +
                                        "-fx-background-radius: 10px; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 4);");
                    
                    emptyMessageBox.getChildren().add(emptyMessage);
                    reclamationsContainer.getChildren().add(emptyMessageBox);
                    reclamationsContainer.setAlignment(javafx.geometry.Pos.CENTER);
                } else {
                    for (Reclamation rec : reclamations) {
                        VBox box = createReclamationBox(rec);
                        VBox.setMargin(box, new Insets(5, 0, 5, 0));
                        reclamationsContainer.getChildren().add(box);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                headerLabel.setText("Erreur lors de la recherche des réclamations");
            }
        });
        try {
            if (user == null) {
                user = new User();
                user.setId(3);
                user.setRole("Student");
            }
            
            List<Reclamation> reclamations = reclamationService.recuperer(user);
            if (reclamations.isEmpty()) {
                VBox emptyMessageBox = new VBox();
                emptyMessageBox.setAlignment(javafx.geometry.Pos.CENTER);
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
                reclamationsContainer.setAlignment(javafx.geometry.Pos.CENTER);
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
            if (user != null && (user.getRole().equals("Student") || user.getRole().equals("Instructor"))) {
                actions.getChildren().add(createButton("Modifier Contenu", "btn-success",
                        e -> openModifierReclamationDialog(rec)));
            }
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
            reclamationContenu.setPrefHeight(200);
            reclamationContenu.setPrefWidth(400);
            reclamationContenu.setStyle("-fx-font-size: 14px; -fx-border-color: #ccc; -fx-border-radius: 5px;");

            Button btnModifier = new Button("Modifier");
            btnModifier.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            btnModifier.setOnAction(event -> {
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Confirmation de modification");
                confirmationAlert.setHeaderText(null);
                confirmationAlert.setContentText("Voulez-vous vraiment modifier la réclamation ?");

                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        String nouveauContenu = reclamationContenu.getText();
                        try {
                            reclamationService.modifier(reclamation.getId(), nouveauContenu);
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Modification réussie");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("Réclamation modifiée avec succès !");
                            successAlert.showAndWait();
                            Stage stage = (Stage) btnModifier.getScene().getWindow();
                            stage.close();
                            initialize();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
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
        if (user == null) {
            user = new User();
            user.setId(3);
        }
        try {
            reclamationService.ArchiverFront(reclamation.getId(), 1);
            List<Reclamation> reclamations = reclamationService.recuperer(user);
            reclamationsContainer.getChildren().clear();

            if (reclamations.isEmpty()) {
                headerLabel.setText("Aucune réclamation pour l'utilisateur " + user.getId());
            } else {
                for (Reclamation rec : reclamations) {
                    VBox box = createReclamationBox(rec);
                    VBox.setMargin(box, new Insets(5, 0, 5, 0));
                    reclamationsContainer.getChildren().add(box);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void envoyerMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            chatArea.appendText("Vous : " + message + "\n");
            inputField.clear();

            new Thread(() -> {
                String response = chatGPT.envoyer(message);
                javafx.application.Platform.runLater(() -> {
                    chatArea.appendText("Chatbot : " + response + "\n");
                });
            }).start();
        }
    }

    @FXML
    private void showHoverMessage() {
        hoverMessage.setText("Résolvez votre problème ici");
        hoverMessage.setVisible(true);
    }

    @FXML
    private void hideHoverMessage() {
        hoverMessage.setVisible(false);
    }

    @FXML
    private void openChat() {
        chatContainer.setVisible(true);
        if (chatArea.getText().isEmpty()) {
            chatArea.appendText("Chatbot : Bonjour ! Comment puis-je vous aider aujourd'hui avec votre problème ?\n");
        }
    }

    @FXML
    private void closeChat() {
        chatContainer.setVisible(false);
    }
}

