package tn.learniverse.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import tn.learniverse.services.LogsService;
import tn.learniverse.services.PosteService;
import tn.learniverse.entities.Poste;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AfficherPosteController implements Initializable {
    public Button ajouterButton1;
    public Button Settingsbtn;
    public Label role;
    public Label usernameLabel;
    public Button logoutButton;
    public ImageView UserPicture;
    @FXML
    private VBox postsContainer; 

    @FXML
    private TextField searchField;
    @FXML
    private ChoiceBox<String> categoryChoiceBox;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startSessionMonitor();
        try {
            ImageView imageView = new ImageView();
            Image image = new Image("file:///C:/wamp64/www/images/icon/logout.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.logoutButton.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        try {
            ImageView imageView = new ImageView();
            Image image = new Image("file:///C:/wamp64/www/images/icon/settings.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.Settingsbtn.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        try {
            if(Session.getCurrentUser()==null){
                ActionEvent event = new ActionEvent();
                Navigator.redirect(event,"/fxml/user/Login.fxml");
            }
            else{
                this.usernameLabel.setText(Session.getCurrentUser().getNom());
                this.role.setText(Session.getCurrentUser().getRole());
                String picturePath = Session.getCurrentUser().getPicture();
                Image image;

                if (picturePath != null) {
                    image = new Image("file:///" + picturePath.replace("\\", "/"), 50, 50, false, false);
                    if(image.isError()){
                        image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 50, 50, false, false);
                    }
                } else {
                    image = new Image("file:///C:/wamp64/www/images/users/user.jpg", 50, 50, false, false);
                }

                this.UserPicture.setImage(image);
                Circle clip = new Circle(25, 25, 25);
                this.UserPicture.setClip(clip);
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
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

    public void Comp(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/competitions_list.fxml");

    }

    public void ToReclamaitons(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Reclamation/DisplayReclamations.fxml");
    }

    public void ToForum(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/AfficherPoste.fxml");
    }

    public void ToDiscover(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/CoursesView.fxml");
    }

    public void ToCourses(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/CoursesView.fxml");
    }

    public void ToOffers(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/CoursesView.fxml");

    }
    private Timeline sessionMonitor;
    public void startSessionMonitor() {
        sessionMonitor = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> {
                    LogsService logsService = new LogsService();
                    if (Session.getCurrentLog() != null) {
                        int currentLogId = Session.getCurrentLog().getId();
                        boolean exists = logsService.logExists(currentLogId);
                        if (!exists) {
                            // Stop the session monitor
                            sessionMonitor.stop();

                            Platform.runLater(() -> {
                                try {
                                    Navigator.showAlert(Alert.AlertType.WARNING, "Session expired", "You have been logged out.");
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/Login.fxml"));
                                    Parent root = loader.load();
                                    Stage stage = (Stage) Stage.getWindows().filtered(Window::isShowing).get(0);
                                    stage.setScene(new Scene(root));
                                    stage.setTitle("Login");
                                    stage.show();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            });
                        }
                    }
                })
        );
        sessionMonitor.setCycleCount(Timeline.INDEFINITE);
        sessionMonitor.play();
    }

    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.showAlert(Alert.AlertType.INFORMATION,"See you soon ","You are going to logout");
        Navigator.redirect(actionEvent,"/fxml/user/Login.fxml");
    }
    public void Settings(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/Settings.fxml");
    }

}

