package tn.learniverse.controllers.Reclamation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import tn.learniverse.entities.Reclamation;
import tn.learniverse.entities.User;
import tn.learniverse.entities.Reponse;
import tn.learniverse.services.ReponseService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

public class AjouterReponseController {

    @FXML
    private TextArea textAreaReponse;

    private Reclamation reclamation;
    private User user;
    private final ReponseService reponseService = new ReponseService();

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
    }

    public void setUser(User user) {
        this.user = user;
    }


    @FXML
    private void handleSubmit() {
        String contenu = textAreaReponse.getText();

        if (contenu.trim().isEmpty()) {
            System.out.println("La réponse ne peut pas être vide.");
            return;
        }

        if (user == null) {
            System.out.println("L'utilisateur n'est pas défini.");
            return;
        }

        Reponse reponse = new Reponse(contenu, new Date(), reclamation, user, 0);

        try {
            reponseService.ajouter(reponse, user, reclamation);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/Reponses.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) textAreaReponse.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/Reponses.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) textAreaReponse.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
