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

public class ReponsesBack {
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
    private static final int ADMIN_USER_ID = 2; // ID utilisateur pour l'admin

    @FXML
    private void initialize() {
        btnRetour.setOnAction(e -> retournerVersDisplayReclamations());
    }

    public void setReclamation(Reclamation rec) throws SQLException {
        this.reclamation = rec;
        labelTitre.setText("Réponses pour : " + rec.getTitre());

        // Utilisateur courant pour l'ajout de nouvelles réponses uniquement
        user = new User();
        user.setId(ADMIN_USER_ID); // Définir l'ID 2 pour l'administrateur
        user.setRole("Admin"); // Définir le rôle comme Admin

        List<Reponse> reponses = rec.getReponses();
        System.out.println("Nombre de réponses : " + reponses.size());

        // Parcourir les réponses et créer des utilisateurs si nécessaire
        for (Reponse reponse : reponses) {
            System.out.println("Réponse ID: " + reponse.getId() +
                    ", Statut: " + reponse.getStatut() +
                    ", User ID: " + (reponse.getUser() != null ? reponse.getUser().getId() : "null") +
                    ", Rôle utilisateur: " + (reponse.getUser() != null ? reponse.getUser().getRole() : "null"));

            // Si l'utilisateur est null, créer un utilisateur par défaut avec ID=2 et rôle Admin
            if (reponse.getUser() == null) {
                User defaultUser = new User();
                defaultUser.setId(ADMIN_USER_ID);
                defaultUser.setRole("Admin");
                defaultUser.setPrenom("Admin");
                defaultUser.setNom("Learniverse");
                reponse.setUser(defaultUser);
            }
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

        // Utiliser l'utilisateur de la réponse pour l'affichage
        User responseUser = reponse.getUser();
        if (responseUser != null && dateReponse != null) {
            String auteur;
            // Vérifier le rôle de l'utilisateur pour l'affichage
            if ("Admin".equals(responseUser.getRole())) {
                auteur = "Learniverse";
            } else {
                // Pour Student ou Instructor, afficher le nom complet
                String prenom = responseUser.getPrenom() != null ? responseUser.getPrenom() : "Inconnu";
                String nom = responseUser.getNom() != null ? responseUser.getNom() : "Inconnu";
                auteur = prenom + " " + nom;
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

        // ----- Gestion des boutons selon le statut et le rôle de l'utilisateur -----
        if (reponse != null) {
            HBox buttonBox = new HBox();
            buttonBox.setSpacing(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);

            User currentUser = this.user; // L'utilisateur actuel (admin avec ID 2)

            // Si l'utilisateur courant est Admin (ID 2)
            if (currentUser != null && currentUser.getId() == ADMIN_USER_ID && "Admin".equals(currentUser.getRole())) {
                // Si le statut est 0, afficher le bouton Répondre
                if (reponse.getStatut() == 0) {
                    Button btnRepondre = new Button("Répondre");
                    btnRepondre.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-background-radius: 5;");
                    btnRepondre.setOnAction(e -> openReponseDialog("répondre", reponse));
                    buttonBox.getChildren().add(btnRepondre);
                }
            }

            // Si l'utilisateur de la réponse est Student ou Instructor et si le statut est 0
            if (responseUser != null &&
                    ("Student".equals(responseUser.getRole()) || "Instructor".equals(responseUser.getRole()))
                    && reponse.getStatut() == 0) {
                Button btnModifier = new Button("Modifier Réponse");
                btnModifier.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-background-radius: 5;");
                btnModifier.setOnAction(e -> openModifierReponseDialog(reponse));
                buttonBox.getChildren().add(btnModifier);
            }

            // Ajouter les boutons seulement s'il y en a
            if (!buttonBox.getChildren().isEmpty()) {
                bubble.getChildren().add(buttonBox);
            }
        }

        HBox wrapper = new HBox(bubble);
        wrapper.setStyle("-fx-alignment: top-left;");
        return wrapper;
    }

    private boolean isDefaultDate(Date date) {
        if (date == null) return true;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String defaultDate = "1970-01-01 00:00:00";
        return sdf.format(date).equals(defaultDate);
    }

    private void openReponseDialog(String action, Reponse reponse) {
        try {
            if ("modifier".equals(action)) {
                openModifierReponseDialog(reponse);
            } else {  // Ajout
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
                // Alerte de confirmation après la modification
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Confirmation de modification");
                confirmationAlert.setHeaderText("Modifier la réponse");
                confirmationAlert.setContentText("Voulez-vous vraiment modifier cette réponse ?");

                ButtonType buttonTypeOui = new ButtonType("Oui", ButtonBar.ButtonData.YES);
                ButtonType buttonTypeNon = new ButtonType("Non", ButtonBar.ButtonData.NO);

                confirmationAlert.getButtonTypes().setAll(buttonTypeOui, buttonTypeNon);

                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == buttonTypeOui) {
                        // Si l'utilisateur confirme, enregistrer les modifications
                        String contenu = textArea.getText();
                        try {
                            reponseService.modifier(reponse.getId(), contenu, reponse.getUser(), reponse.getReclamation());
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
                if (contenu.trim().isEmpty()) {
                    System.out.println("La réponse ne peut pas être vide.");
                    return;
                }
                try {
                    // Créer une nouvelle réponse avec l'utilisateur admin (ID=2)
                    Reponse reponse = new Reponse(contenu, new Date(), reclamation, user, 0);
                    reponseService.ajouter(reponse, user, reclamation);

                    // Assurer que l'utilisateur est correctement défini
                    reponse.setUser(user);
                    reponse.setStatut(1); // Mettre à jour le statut

                    // Ajouter la réponse à la liste observable et rafraîchir l'affichage
                    observableReponses.add(reponse);
                    refreshReponses();
                    stage.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/DisplayReclamationBack.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}