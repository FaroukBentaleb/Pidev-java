package tn.learniverse.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import tn.learniverse.services.PosteService;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.event.ActionEvent;
import tn.learniverse.entities.Poste;
import tn.learniverse.services.PosteService;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.stage.FileChooser;


import java.util.List;
import java.util.ResourceBundle;

public class AfficherPosteController implements Initializable {

    @FXML
    private VBox postsContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PosteService ps = new PosteService();
        List<Poste> postes = ps.getAll();

        for (Poste p : postes) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PosteItem.fxml"));
                Parent posteCard = loader.load();

                PosteItemController controller = loader.getController();
                controller.setData(p);

                postsContainer.getChildren().add(posteCard);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
