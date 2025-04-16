package tn.learniverse.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.learniverse.services.PosteService;
import tn.learniverse.entities.Poste;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AfficherPosteController implements Initializable {
    @FXML
    private VBox postsContainer; // Doit correspondre à votre FXML

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chargerPosts();
    }

    private void chargerPosts() {
        postsContainer.getChildren().clear();
        List<Poste> posts = new PosteService().getAll();

        for (Poste post : posts) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PosteItem.fxml"));
                Parent postItem = loader.load();
                PosteItemController controller = loader.getController();
                controller.setData(post, this::chargerPosts);
                postsContainer.getChildren().add(postItem);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

