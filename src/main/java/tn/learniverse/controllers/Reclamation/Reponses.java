package tn.learniverse.controllers.Reclamation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.learniverse.entities.Reclamation;
import tn.learniverse.entities.Reponse;
import tn.learniverse.entities.User;
import tn.learniverse.services.FlaskClient;
import tn.learniverse.services.ReclamationService;
import tn.learniverse.services.ReponseService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.learniverse.tools.Session;

public class Reponses {

    @FXML
    private Label labelTitre;
    @FXML
    private VBox vboxReponses;
    @FXML
    private TextArea textAreaReponse;
    @FXML
    private Button btnRetour;

    private final ReclamationService reclamationService = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();
    private Reclamation reclamation;
    private User user;
    private ObservableList<Reponse> observableReponses;

    @FXML
    private void initialize() {
        btnRetour.setOnAction(e -> retournerVersDisplayReclamations());
    }

    public void setReclamation(Reclamation rec) throws SQLException {
        this.reclamation = rec;
        labelTitre.setText("Réponses pour : " + rec.getTitre());
        user = Session.getCurrentUser();
        /*user = new User();
        user.setId(3);*/
        user.setPrenom(rec.getUser().getPrenom());
        user.setNom(rec.getUser().getNom());
        user.setRole(rec.getUser().getRole());
        user.setEmail(rec.getUser().getEmail());

        List<Reponse> reponses = rec.getReponses();
        for (Reponse reponse : reponses) {
            user = Session.getCurrentUser();
            reponse.setUser(user);
            /*if (reponse.getUser() == null) {
                reponse.setUser(user);
            }*/
        }
        observableReponses = FXCollections.observableArrayList(reponses);
        refreshReponses();
    }

    private Node createBubble(String message, User user, Date dateReponse, Date dateModification, Reponse reponse) {
        VBox bubble = new VBox();
        bubble.setStyle("-fx-background-color: #f0f4f8; -fx-padding: 20; -fx-background-radius: 15; -fx-border-color: #d1d9e6; -fx-border-width: 1; -fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0.5, 0, 4);");
        bubble.setPrefWidth(800);
        bubble.setMaxWidth(1200);
        bubble.setMinWidth(900);
        bubble.setSpacing(10);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        User responseUser = reponse.getUser();
        if (responseUser != null && dateReponse != null) {
            String auteur;
            if ("Admin".equals(responseUser.getRole())) {
                auteur = "Admin Learniverse";
            } else {
                String prenom = responseUser.getPrenom() != null ? responseUser.getPrenom() : "Inconnu";
                String nom = responseUser.getNom() != null ? responseUser.getNom() : "Inconnu";
                String role = responseUser.getRole() != null ? responseUser.getRole() : "Inconnu";
                auteur = prenom + " " + nom + " (" + role + ")";
            }

            String dateFormatted = dateFormat.format(dateReponse);

            Label header = new Label("Répondu par: " + auteur + " le " + dateFormatted);
            header.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a73e8; -fx-font-size: 16px;");
            bubble.getChildren().add(header);

            if (dateModification != null && !isDefaultDate(dateModification)) {
                String dateModifFormatted = dateFormat.format(dateModification);
                Label modifLabel = new Label("Modifiée par: " + auteur + " le " + dateModifFormatted);
                modifLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666; -fx-font-size: 14px;");
                bubble.getChildren().add(modifLabel);
            }
        }

        Label content = new Label(message);
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");
        bubble.getChildren().add(content);

        if (reponse != null && reponse.getStatut() == 0) {
            HBox buttonBox = new HBox();
            buttonBox.setSpacing(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);

            if (responseUser != null && !"Admin".equals(responseUser.getRole())) {
                Button btnModifier = new Button("Modifier Réponse");
                btnModifier.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-background-radius: 5;");
                btnModifier.setOnAction(e -> openModifierReponseDialog(reponse));
                buttonBox.getChildren().add(btnModifier);
            }
            else {
                Button btnRepondre = new Button("Répondre");
                btnRepondre.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-background-radius: 5;");
                btnRepondre.setOnAction(e -> openReponseDialog("répondre", reponse));
                buttonBox.getChildren().add(btnRepondre);
            }
            if (!buttonBox.getChildren().isEmpty()) {
                bubble.getChildren().add(buttonBox);
            }
        }

        HBox wrapper = new HBox(bubble);
        wrapper.setStyle("-fx-alignment: top-left;");
        return wrapper;
    }

    private boolean isDefaultDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String defaultDate = "1970-01-01 00:00:00";
        return sdf.format(date).equals(defaultDate);
    }

    private void openReponseDialog(String action, Reponse reponse) {
        try {
            if ("modifier".equals(action)) {
                openModifierReponseDialog(reponse);
            } else {
                openAjouterReponseDialog();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshReponses() {
        vboxReponses.getChildren().clear();
        for (Reponse reponse : observableReponses) {
            vboxReponses.getChildren().add(createBubble(reponse.getContenu(), reponse.getUser(), reponse.getDateReponse(), reponse.getDateModification(), reponse));
        }
    }

    private void openModifierReponseDialog(Reponse reponse) {
        try {
            Stage stage = new Stage();
            VBox vbox = new VBox(15);
            vbox.setPadding(new Insets(30));
            vbox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d9e6; -fx-border-width: 1; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 4);");

            TextArea textArea = new TextArea(reponse.getContenu());
            textArea.setPrefSize(500, 250);
            textArea.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-background-color: #f9f9f9; -fx-border-color: #ccc; -fx-border-radius: 5;");

            Button btnSave = new Button("Enregistrer");
            btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5;");
            btnSave.setOnAction(e -> {
                String contenu = textArea.getText();
                boolean test = FlaskClient.containsBadWords(contenu);
                if (test) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Attention");
                    alert.setHeaderText(null);
                    alert.setContentText("Votre contenu contient des mots inappropriés. Veuillez corriger votre réponse.");
                    alert.showAndWait();
                    return;
                }
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Confirmation de modification");
                confirmationAlert.setHeaderText("Modifier la réponse");
                confirmationAlert.setContentText("Voulez-vous vraiment modifier cette réponse ?");

                ButtonType buttonTypeOui = new ButtonType("Oui", ButtonBar.ButtonData.YES);
                ButtonType buttonTypeNon = new ButtonType("Non", ButtonBar.ButtonData.NO);

                confirmationAlert.getButtonTypes().setAll(buttonTypeOui, buttonTypeNon);

                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == buttonTypeOui) {
                        String contenu1  = textArea.getText();
                        try {
                            reponseService.modifier(reponse.getId(), contenu1  , reponse.getUser(), reponse.getReclamation());
                            reponse.setContenu(contenu);
                            reponse.setDateModification(new Date());
                            refreshReponses();
                            stage.close();

                            // Afficher l'alerte après la modification
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Modification réussie");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("La réponse a été modifiée avec succès.");
                            successAlert.showAndWait();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            });

            Button btnCancel = new Button("Annuler");
            btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5;");
            btnCancel.setOnAction(e -> stage.close());

            HBox buttons = new HBox(15, btnSave, btnCancel);
            buttons.setAlignment(Pos.CENTER);

            vbox.getChildren().addAll(textArea, buttons);

            Scene scene = new Scene(vbox);
            stage.setScene(scene);
            stage.setTitle("Modifier la Réponse");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openAjouterReponseDialog() {
        try {
            Stage stage = new Stage();
            VBox vbox = new VBox(15);
            vbox.setPadding(new Insets(30));
            vbox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d9e6; -fx-border-width: 1; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 4);");

            TextArea textArea = new TextArea();
            textArea.setPrefSize(500, 250);
            textArea.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-background-color: #f9f9f9; -fx-border-color: #ccc; -fx-border-radius: 5;");

            Button btnSubmit = new Button("Soumettre");
            btnSubmit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5;");
            btnSubmit.setOnAction(e -> {
                String contenu = textArea.getText();
                boolean test = FlaskClient.containsBadWords(contenu);
                if (test) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Attention");
                    alert.setHeaderText(null);
                    alert.setContentText("Votre contenu contient des mots inappropriés. Veuillez corriger votre réponse.");
                    alert.showAndWait();
                    return;
                }
                if (contenu.trim().isEmpty()) {
                    System.out.println("La réponse ne peut pas être vide.");
                    return;
                }
                try {
                    if (!observableReponses.isEmpty()) {
                        Reponse lastReponse = observableReponses.get(observableReponses.size() - 1);
                        lastReponse.setStatut(1);
                        reponseService.updateStatut(lastReponse.getId(), 1);
                    }
                    /*if (user.getPrenom() == null || user.getNom() == null || user.getRole() == null) {
                        user.setPrenom(reclamation.getUser().getPrenom());
                        user.setNom(reclamation.getUser().getNom());
                        user.setRole(reclamation.getUser().getRole());
                        user.setEmail(reclamation.getUser().getEmail());
                    }*/
                    user = Session.getCurrentUser();
                    user.setPrenom(reclamation.getUser().getPrenom());
                    user.setNom(reclamation.getUser().getNom());
                    user.setRole(reclamation.getUser().getRole());
                    user.setEmail(reclamation.getUser().getEmail());

                    Reponse reponse = new Reponse(contenu, new Date(), reclamation, user, 0);
                    reponseService.ajouter(reponse, user, reclamation);
                    reponse.setUser(user);
                    observableReponses.add(reponse);
                    refreshReponses();
                    stage.close();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Succès");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("La réponse a été ajoutée avec succès.");
                    successAlert.showAndWait();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Une erreur est survenue lors de l'ajout de la réponse.");
                    errorAlert.showAndWait();
                }
            });

            Button btnCancel = new Button("Annuler");
            btnCancel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5;");
            btnCancel.setOnAction(e -> stage.close());

            HBox buttons = new HBox(15, btnSubmit, btnCancel);
            buttons.setAlignment(Pos.CENTER);

            vbox.getChildren().addAll(textArea, buttons);

            Scene scene = new Scene(vbox);
            stage.setScene(scene);
            stage.setTitle("Ajouter une Réponse");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void retournerVersDisplayReclamations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/DisplayReclamations.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
