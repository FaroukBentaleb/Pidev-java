package tn.learniverse.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/DisplayReclamations.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find FXML file at /Reclamation/Back.fxml");
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Learniverse");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
