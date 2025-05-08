package tn.learniverse.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.learniverse.services.PosteService;
import tn.learniverse.entities.Poste;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AfficherPosteController implements Initializable {
    public Button ajouterButton1;
    @FXML
    private VBox postsContainer; 

    @FXML
    private TextField searchField;
    @FXML
    private ChoiceBox<String> categoryChoiceBox;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initCategoryChoiceBox();
        chargerPosts();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            rechercherPostesParTitre(newValue);
        });

        categoryChoiceBox.setOnAction(e -> filtrerParCategorie());


    }

    private void initCategoryChoiceBox() {
        categoryChoiceBox.getItems().addAll("All", "Digital Marketing", "Health & Fitness", "Programming & Tech", "Product Design");
        categoryChoiceBox.setValue("All");
    }

    private void filtrerParCategorie() {
        String selectedCategory = categoryChoiceBox.getValue();
        postsContainer.getChildren().clear();

        List<Poste> posts = new PosteService().getAll();

        for (Poste post : posts) {
            if (selectedCategory.equals("All") || post.getCategorie().equalsIgnoreCase(selectedCategory)) {
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

    private void rechercherPostesParTitre(String filtre) {
        postsContainer.getChildren().clear();
        List<Poste> posts = new PosteService().getAll(); // Tu peux optimiser plus tard en mettant ça dans un cache

        for (Poste post : posts) {
            if (post.getTitre().toLowerCase().contains(filtre.toLowerCase())) {
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


    @FXML
    void AjoutPoste1(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPoste.fxml"));
            Parent root = loader.load();

            AjouterPosteController controller = loader.getController();
            controller.setRefreshCallback(this::chargerPosts); // méthode de rafraîchissement

            Stage stage = new Stage();
            stage.setTitle("Ajouter un post");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}

