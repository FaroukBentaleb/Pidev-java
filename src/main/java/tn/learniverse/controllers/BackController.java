package tn.learniverse.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class BackController {
    
    @FXML
    private Button ReclamationArchivées;
    
    @FXML
    private VBox reclamationsContainer;
    
    @FXML
    public void afficherReclamationsArchivees() {
        // Implémentez ici la logique pour afficher les réclamations archivées
        System.out.println("Affichage des réclamations archivées");
        // TODO: Ajouter la logique pour charger et afficher les réclamations archivées
    }
} 