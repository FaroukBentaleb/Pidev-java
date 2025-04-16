package tn.learniverse.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.learniverse.entities.Reclamation;
import tn.learniverse.entities.Reponse;
import tn.learniverse.entities.User;
import tn.learniverse.services.ReclamationService;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Reponses {

    @FXML private ListView<String> listReponses;
    @FXML private Label labelTitre;
    private final ReclamationService reclamationService = new ReclamationService();
    @FXML private VBox vboxReponses;

    public void setReclamation(Reclamation rec) throws SQLException {
        labelTitre.setText("Réponses pour : " + rec.getTitre());

        List<Reponse> reponses = rec.getReponses();

        if (reponses == null || reponses.isEmpty()) {
            vboxReponses.getChildren().add(createBubble("Aucune réponse disponible.", null, null));
        } else {
            for (Reponse reponse : reponses) {
                vboxReponses.getChildren().add(
                        createBubble(reponse.getContenu(), reponse.getUser(), reponse.getDateReponse())
                );
            }
        }
    }

    private Node createBubble(String message, User user, Date date) {
        VBox bubble = new VBox();
        bubble.setStyle("-fx-background-color: #e8f0fe; -fx-padding: 10; -fx-background-radius: 10;");
        bubble.setMaxWidth(800);
        bubble.setSpacing(5);

        if (user != null && date != null) {
            String auteur;
            // Use equals() to check if the role is exactly "Admin"
            if ("Admin".equals(user.getRole())) {
                auteur = "Learniverse";
            } else {
                auteur = user.getPrenom() + " " + user.getNom();
            }

            String dateFormatted = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);
            Label header = new Label("Répondu par: " + auteur + " le " + dateFormatted);
            header.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a73e8;");
            bubble.getChildren().add(header);
        }

        Label content = new Label(message);
        content.setWrapText(true);
        bubble.getChildren().add(content);

        HBox wrapper = new HBox(bubble);
        wrapper.setStyle("-fx-alignment: top-left;");

        return wrapper;
    }


    private String formatReponse(Reponse reponse) {
        String dateReponse = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(reponse.getDateReponse());
        return "Le " + dateReponse + ": " + reponse.getContenu();
    }
}
