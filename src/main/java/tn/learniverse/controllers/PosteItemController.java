package tn.learniverse.controllers;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.learniverse.entities.Poste;
import javafx.scene.control.Label;
import java.net.URLEncoder;

public class PosteItemController {

    @FXML
    private Label auteurLabel;

    @FXML
    private Label categorieLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label nbCommentairesLabel;

    @FXML
    private Label titreLabel;

    @FXML
    private Label contenuLabel;

    @FXML
    private ImageView photoImageView;

    public void setData(Poste poste) {
        if (poste.getUser() != null) {
            auteurLabel.setText(poste.getUser().getPrenom() + " " + poste.getUser().getNom());
        }

        categorieLabel.setText(poste.getCategorie());
        dateLabel.setText(poste.getDatePost());
        nbCommentairesLabel.setText(poste.getNbCom() + " comments");
        titreLabel.setText(poste.getTitre());
        contenuLabel.setText(poste.getContenu());

        if (poste.getPhoto() != null && !poste.getPhoto().isEmpty()) {
            try {
                // Solution optimale pour Windows :
                String photoPath = "file:///C:/wamp64/www/" + poste.getPhoto().replace(" ", "%20");

                // Alternative plus robuste :
                // String photoPath = new File("C:/wamp64/www/" + poste.getPhoto()).toURI().toString();

                System.out.println("URL de l'image test : " + photoPath);

                Image image = new Image(photoPath, true); // true = chargement asynchrone
                photoImageView.setImage(image);

                // Vérification du chargement
                image.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        System.out.println("ERREUR: Impossible de charger l'image depuis: " + photoPath);
                        photoImageView.setVisible(false);
                    }
                });


            } catch (Exception e) {
                System.out.println("Erreur chargement image: " + e.getMessage());
            }
        }
    }
}
