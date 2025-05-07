package tn.learniverse.controllers.Reclamation;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import javafx.event.ActionEvent;
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
import tn.learniverse.services.EmailService;
import tn.learniverse.services.ReclamationService;
import tn.learniverse.services.ReponseService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.learniverse.tools.Navigator;

import javax.mail.MessagingException;

public class ReponsesBack {
    public static final String ACCOUNT_SID = "ACa5b91cf8110a06dfde772f780d6f4e5a";
    public static final String AUTH_TOKEN = "21d4ea3471c5a8161b289db48a10674e";
    @FXML
    private Label labelTitre;
    @FXML
    private VBox vboxReponses;
    @FXML
    private TextArea textAreaReponse;
    @FXML
    private Button btnRetour;
    @FXML
    private Button reclamationTraite;

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
        if ("Traité".equals(rec.getStatut())) {
            reclamationTraite.setVisible(false);
            reclamationTraite.setManaged(false);
        }
        
        user = new User();
        user.setId(2);
        user.setRole("Admin");

        List<Reponse> reponses = rec.getReponses();
        for (Reponse reponse : reponses) {
            if (reponse.getUser() == null) {
                User defaultUser = new User();
                defaultUser.setId(2);
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

        User responseUser = reponse.getUser();
        if (responseUser != null && dateReponse != null) {
            String auteur;
            if ("Admin".equals(responseUser.getRole())) {
                auteur = "Learniverse";
            } else {
                String prenom = responseUser.getPrenom() != null ? responseUser.getPrenom() : "Inconnu";
                String nom = responseUser.getNom() != null ? responseUser.getNom() : "Inconnu";
                String role = responseUser.getRole() != null ? responseUser.getRole() : "Inconnu";
                auteur = prenom + " " + nom + " ";
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

            if (responseUser != null && "Admin".equals(responseUser.getRole())) {
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
        if (date == null) return true;
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
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Confirmation de modification");
                confirmationAlert.setHeaderText("Modifier la réponse");
                confirmationAlert.setContentText("Voulez-vous vraiment modifier cette réponse ?");

                ButtonType buttonTypeOui = new ButtonType("Oui", ButtonBar.ButtonData.YES);
                ButtonType buttonTypeNon = new ButtonType("Non", ButtonBar.ButtonData.NO);

                confirmationAlert.getButtonTypes().setAll(buttonTypeOui, buttonTypeNon);

                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == buttonTypeOui) {
                        String contenu = textArea.getText();
                        try {
                            reponseService.modifier(reponse.getId(), contenu, reponse.getUser(), reponse.getReclamation());
                            reponse.setContenu(contenu);
                            reponse.setDateModification(new Date());
                            refreshReponses();
                            stage.close();
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Modification réussie");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("La réponse a été modifiée avec succès.");
                            successAlert.showAndWait();
                            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
                            String formattedPhoneNumber = "+216" + reclamation.getUser().getTel();
                            Message message = Message.creator(
                                            new PhoneNumber("+21658407572"), // To number
                                            new PhoneNumber("+19786985856"), // From Twilio number
                                            "vous avez une réponse sur votre reclamation !")
                                    .create();
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
                                    contenu
                            );

                            try {
                                EmailService.sendEmail(
                                        reclamation.getUser().getEmail(),
                                        "Réponse à votre réclamation - " + reclamation.getTitre(),
                                        emailContent
                                );
                            } catch (MessagingException emailError) {
                                System.err.println("Erreur lors de l'envoi de l'email : " + emailError.getMessage());
                            }
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
                    if (!observableReponses.isEmpty()) {
                        Reponse lastReponse = observableReponses.get(observableReponses.size() - 1);
                        lastReponse.setStatut(1);
                        reponseService.updateStatut(lastReponse.getId(), 1);
                    }
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
                    Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
                    String formattedPhoneNumber = "+216" + reclamation.getUser().getTel();
                    Message message = Message.creator(
                                    new PhoneNumber("+21658407572"), // To number
                                    new PhoneNumber("+19786985856"), // From Twilio number
                                    "vous avez une réponse sur votre reclamation !")
                            .create();
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
                            contenu
                    );

                    try {
                        EmailService.sendEmail(
                                reclamation.getUser().getEmail(),
                                "Réponse à votre réclamation - " + reclamation.getTitre(),
                                emailContent
                        );
                    } catch (MessagingException emailError) {
                        System.err.println("Erreur lors de l'envoi de l'email : " + emailError.getMessage());
                    }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/DisplayReclamationBack.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void marquerReclamationTraitee(ActionEvent actionEvent) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Voulez-vous vraiment marquer cette réclamation comme traitée ?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ReclamationService reclamationService = new ReclamationService();
                reclamationService.updateStatut(reclamation.getId(), "Traité");

                ReponseService reponseService = new ReponseService();
                List<Reponse> reponses = reclamation.getReponses();
                if (!reponses.isEmpty()) {
                    Reponse derniereReponse = reponses.get(reponses.size() - 1);
                    reponseService.updateStatut(derniereReponse.getId(), 1);
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/DisplayReclamationBack.fxml"));
                Parent root = loader.load();
                Scene scene = ((Node) actionEvent.getSource()).getScene();
                scene.setRoot(root);

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("La réclamation a été marquée comme traitée avec succès.");
                successAlert.showAndWait();

            } catch (SQLException | IOException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Une erreur est survenue lors de la mise à jour du statut : " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }

    public void Dashboard(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Dashboard.fxml");
    }

    public void complaints(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Reclamation/DisplayReclamationBack.fxml");
    }
}