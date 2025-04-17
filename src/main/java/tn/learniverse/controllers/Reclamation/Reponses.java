package tn.learniverse.controllers.Reclamation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.learniverse.entities.Reclamation;
import tn.learniverse.entities.Reponse;
import tn.learniverse.entities.User;
import tn.learniverse.services.ReclamationService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Reponses {

    @FXML
    private Label labelTitre;
    @FXML
    private VBox vboxReponses;

    private final ReclamationService reclamationService = new ReclamationService();
    private Reclamation reclamation;
    private User user;

    public void setReclamation(Reclamation rec) throws SQLException {
        labelTitre.setText("Réponses pour : " + rec.getTitre());

        List<Reponse> reponses = rec.getReponses();

        if (reponses == null || reponses.isEmpty()) {
            vboxReponses.getChildren().add(createBubble("Aucune réponse disponible.", null, null, null, null));
        } else {
            for (Reponse reponse : reponses) {
                vboxReponses.getChildren().add(
                        createBubble(
                                reponse.getContenu(),
                                reponse.getUser(),
                                reponse.getDateReponse(),
                                reponse.getDateModification(),
                                reponse
                        )
                );
            }
        }
    }

    private Node createBubble(String message, User user, Date dateReponse, Date dateModification, Reponse reponse) {
        VBox bubble = new VBox();
        bubble.setStyle("-fx-background-color: #e8f0fe; -fx-padding: 10; -fx-background-radius: 10;");
        bubble.setMaxWidth(800);
        bubble.setSpacing(5);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        if (user != null && dateReponse != null) {
            String auteur = "Admin".equals(user.getRole()) ? "Learniverse" : user.getPrenom() + " " + user.getNom();
            String dateFormatted = dateFormat.format(dateReponse);

            Label header = new Label("Répondu par: " + auteur + " le " + dateFormatted);
            header.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a73e8;");
            bubble.getChildren().add(header);

            if (dateModification != null && !isDefaultDate(dateModification)) {
                String dateModifFormatted = dateFormat.format(dateModification);
                Label modifLabel = new Label("Modifiée par: " + auteur + " le " + dateModifFormatted);
                modifLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");

                bubble.getChildren().add(modifLabel);
            }
        }

        Label content = new Label(message);
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 14;");
        bubble.getChildren().add(content);

        // ----- Boutons selon le statut et rôle -----
        if (reponse != null && reponse.getStatut() == 0) {
            HBox buttonBox = new HBox();
            buttonBox.setSpacing(10);
            buttonBox.setStyle("-fx-alignment: center-right;");

            if ("Admin".equals(reponse.getUser().getRole())) {
                Button btnRepondre = new Button("Répondre");
                btnRepondre.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white;");
                btnRepondre.setOnAction(e -> openReponseDialog("répondre", reponse)); // Passer l'objet Reponse
                buttonBox.getChildren().add(btnRepondre);
            }

            if ("Student".equals(reponse.getUser().getRole()) || "Instructor".equals(reponse.getUser().getRole())) {
                Button btnModifier = new Button("Modifier Réponse");
                btnModifier.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white;");
                btnModifier.setOnAction(e -> openReponseDialog("modifier", reponse)); // Passer l'objet Reponse
                buttonBox.getChildren().add(btnModifier);
            }

            bubble.getChildren().add(buttonBox);
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
            FXMLLoader loader;
            Parent root;

            if ("modifier".equals(action)) {
                loader = new FXMLLoader(getClass().getResource("/Reclamation/ModifierReponse.fxml"));
                root = loader.load();
                ModifierReponseController controller = loader.getController();
                controller.setReponse(reponse);  // Passer l'objet Reponse ici
            } else {  // Ajout
                loader = new FXMLLoader(getClass().getResource("/Reclamation/AjouterReponse.fxml"));
                root = loader.load();
                AjouterReponseController controller = loader.getController();
                controller.setReclamation(reponse.getReclamation());
                controller.setUser(reponse.getUser());
            }

            Stage dialogStage = new Stage();
            dialogStage.setTitle(action.equals("modifier") ? "Modifier la réponse" : "Ajouter une réponse");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshReponses() {
        vboxReponses.getChildren().clear();
        List<Reponse> reponses = reclamation.getReponses(); // Récupérer à nouveau les réponses
        for (Reponse reponse : reponses) {
            vboxReponses.getChildren().add(createBubble(reponse.getContenu(), reponse.getUser(), reponse.getDateReponse(), reponse.getDateModification(), reponse));
        }
    }

}
