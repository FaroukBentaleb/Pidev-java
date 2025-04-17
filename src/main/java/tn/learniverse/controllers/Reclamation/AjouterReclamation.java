package tn.learniverse.controllers.Reclamation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.learniverse.entities.Reclamation;
import tn.learniverse.entities.User;
import tn.learniverse.services.ReclamationService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class AjouterReclamation {

    @FXML
    private Button ReclamationAjouter;

    @FXML
    private TextField reclamationContenu;

    @FXML
    private ImageView reclamationImg;

    @FXML
    private TextField reclamationtTitre;

    @FXML
    private TextField textFieldFilePath;

    private ReclamationService reclamationService = new ReclamationService();

    @FXML
    public void ajouterReclamation(ActionEvent event) {
        String titre = reclamationtTitre.getText();
        String contenu = reclamationContenu.getText();
        Reclamation reclamation = new Reclamation();
        reclamation.setTitre(titre);
        reclamation.setContenu(contenu);
        User user = new User();
        user.setId(3);
        try {
            reclamationService.ajouter(reclamation, user);
            System.out.println("Réclamation ajoutée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/Reponses.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ReclamationAjouter.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
