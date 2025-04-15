package tn.learniverse.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import tn.learniverse.entities.Reclamation;
import tn.learniverse.entities.Reponse;
import tn.learniverse.services.ReponseService;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class Reponses {

    @FXML private ListView<String> listReponses;
    @FXML private Label labelTitre;

    private final ReponseService reponseService = new ReponseService();

    public void setReclamation(Reclamation rec) {
        labelTitre.setText("Réponses pour : " + rec.getTitre());

        try {
            List<Reponse> reponses = reponseService.recuperer(rec);
            if (reponses.isEmpty()) {
                listReponses.getItems().add("Aucune réponse disponible.");
            }
            for (Reponse reponse : reponses) {
                listReponses.getItems().add(formatReponse(reponse));
            }
        } catch (SQLException e) {
            listReponses.getItems().add("Erreur lors du chargement des réponses.");
            e.printStackTrace();
        }
    }


    private String formatReponse(Reponse reponse) {
        String dateReponse = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(reponse.getDateReponse());
        return "Le " + dateReponse + ": " + reponse.getContenu();
    }
}
