package tn.learniverse.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {
    private static Stage primaryStage; // Gardez une référence à la fenêtre principale

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showAjouterPosteScene(); // Affiche d'abord l'interface d'ajout
        stage.setTitle("Learniverse - Forum");
        stage.show();
    }

    // Méthode pour afficher la scène d'ajout
    public static void showAjouterPosteScene() throws IOException {
        Parent root = FXMLLoader.load(MainFX.class.getResource("/AjouterPoste.fxml"));
        primaryStage.setScene(new Scene(root));
    }

    // Méthode pour afficher la scène d'affichage des posts
    public static void showAfficherPostesScene() throws IOException {
        Parent root = FXMLLoader.load(MainFX.class.getResource("/AfficherPoste.fxml"));
        primaryStage.setScene(new Scene(root));
    }
}