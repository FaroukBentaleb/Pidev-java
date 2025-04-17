package tn.learniverse.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import tn.learniverse.entities.Commentaire;
import tn.learniverse.entities.Poste;
import tn.learniverse.services.CommentaireService;
import tn.learniverse.services.PosteService;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class back implements Initializable {

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
        TableColumn<Poste, Void> commentCol = new TableColumn<>("Commentaires");
        commentCol.setPrefWidth(130);
        commentCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Voir commentaires");

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
            private final Button deleteButton = new Button("Supprimer");

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





}



