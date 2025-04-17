package tn.learniverse.controllers.Reclamation;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import tn.learniverse.entities.Reponse;
import tn.learniverse.services.ReponseService;

import java.sql.SQLException;

public class ModifierReponseController {

    @FXML
    private TextArea textAreaReponse;

    private Reponse reponse;
    private final ReponseService reponseService = new ReponseService();

    public void setReponse(Reponse reponse) {
        this.reponse = reponse;
        textAreaReponse.setText(reponse.getContenu());
    }

    @FXML
    private void handleSave() {
        String contenu = textAreaReponse.getText();
        try {
            reponseService.modifier(reponse.getId(), contenu, reponse.getUser(), reponse.getReclamation());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {

    }
}
