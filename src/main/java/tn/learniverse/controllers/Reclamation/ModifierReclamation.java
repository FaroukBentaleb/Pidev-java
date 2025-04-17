package tn.learniverse.controllers.Reclamation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import tn.learniverse.entities.Reclamation;
import tn.learniverse.services.ReclamationService;

import java.sql.SQLException;

public class ModifierReclamation {

    @FXML
    private TextArea ReclamationContenu;

    @FXML
    private Button ReclamationAnnuler;

    @FXML
    private Button ReclamationModifier;

    private Reclamation reclamation;
    private final ReclamationService reclamationService = new ReclamationService();

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
        ReclamationContenu.setText(reclamation.getContenu());
    }

    @FXML
    private void handleSave() {
        String nouveauContenu = ReclamationContenu.getText();
        try {
            reclamationService.modifier(reclamation.getId(), nouveauContenu);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
    }
}
