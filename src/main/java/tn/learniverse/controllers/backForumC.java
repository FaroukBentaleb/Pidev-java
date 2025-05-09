package tn.learniverse.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;
import tn.learniverse.entities.Commentaire;
import tn.learniverse.entities.Poste;
import tn.learniverse.services.CommentaireService;
import tn.learniverse.services.PosteService;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

public class backForumC implements Initializable {

    public Button logoutButton;
    public Button Profilebtn;
    public Label FirstLetter;
    public Circle circleProfile;
    @FXML
    private TableView<Commentaire> tableCommentaires;
    @FXML
    private TableColumn<Commentaire, Integer> colIdCommentaire;
    @FXML
    private TableColumn<Commentaire, String> colContenu;
    @FXML
    private TableColumn<Commentaire, String> colAuteur;

    @FXML
    private TableView<Poste> tableView;

    @FXML
    private TableColumn<Poste, Integer> id;

    @FXML
    private TableColumn<Poste, String> username;

    @FXML
    private TableColumn<Poste, String> titre;

    @FXML
    private TableColumn<Poste, String> contenu;

    @FXML
    private TableColumn<Poste, String> date;

    @FXML
    private TableColumn<Poste, Integer> nbCom;

    @FXML
    private Label navUsernameLabel;

    @FXML
    private TextField searchField;

    private final PosteService posteService = new PosteService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
            Image image = new Image("file:///C:/wamp64/www/images/icon/profile.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.Profilebtn.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        if(Session.getCurrentUser()!=null){
            this.navUsernameLabel.setText(Session.getCurrentUser().getNom());
            this.FirstLetter.setText(Session.getCurrentUser().getNom().toUpperCase().substring(0, 1));
            Random random = new Random();
            Color randomColor = Color.rgb(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
            );
            circleProfile.setFill(randomColor);
        }
        // Configuration des colonnes pour les posts
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        username.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(() ->
                        cellData.getValue().getUser().getNom()));
        titre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        contenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        date.setCellValueFactory(new PropertyValueFactory<>("datePost"));
        nbCom.setCellValueFactory(new PropertyValueFactory<>("nbCom"));

        // Configuration des colonnes pour les commentaires
        colIdCommentaire.setCellValueFactory(new PropertyValueFactory<>("id"));
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colAuteur.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUser().getNom()));

        // Chargement des données des posts
        ObservableList<Poste> postList = FXCollections.observableArrayList(posteService.getAll());
        tableView.setItems(postList);

        // Ajouter la colonne d'action (bouton Voir commentaires)
        addActionColumnToPosts();
    }

    public void afficherCommentairesDuPoste(Poste poste) {
        CommentaireService cs = new CommentaireService();
        List<Commentaire> commentaires = cs.getByPosteId(poste.getId());

        ObservableList<Commentaire> data = FXCollections.observableArrayList(commentaires);
        tableCommentaires.setItems(data);

        // Optionnel: scroll vers la table des commentaires
        tableCommentaires.requestFocus();
    }


    private void addActionColumnToPosts() {
        // Bouton Voir commentaires
        TableColumn<Poste, Void> commentCol = new TableColumn<>("Comments");
        commentCol.setPrefWidth(130);
        commentCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("See comments");

            {
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btn.setOnAction(event -> {
                    Poste poste = getTableView().getItems().get(getIndex());
                    afficherCommentairesDuPoste(poste);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        // Bouton Supprimer
        TableColumn<Poste, Void> deleteCol = new TableColumn<>("Action");
        deleteCol.setPrefWidth(100);
        deleteCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                deleteButton.setOnAction(event -> {
                    Poste poste = getTableView().getItems().get(getIndex());

                    // Supprimer de la base
                    posteService.supprimer(poste.getId());

                    // Supprimer de la table
                    getTableView().getItems().remove(poste);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        // Ajouter les deux colonnes à la table
        tableView.getColumns().addAll(commentCol, deleteCol);
    }


    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.redirect(actionEvent,"/fxml/user/login.fxml");
    }

    public void usersButton(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/usersBack.fxml");
    }

    public void Profile(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/ProfileBack.fxml");
    }
    public void Comp(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/backoffice_competitions.fxml");
    }

    public void ToReclamations(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Reclamation/DisplayReclamationBack.fxml");
    }

    public void ToForums(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/BackForum.fxml");
    }

    public void ToCourses(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/BackCourses.fxml");
    }

    public void ToOffers(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/BackCourses.fxml");
    }

    public void ToDash(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/Back.fxml");
    }


}



