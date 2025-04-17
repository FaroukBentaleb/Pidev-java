package com.esprit.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.esprit.java.controllers.BackofficeCompetitionsController;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the backoffice view directly
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/java/backoffice_competitions.fxml"));
        Parent root = loader.load();
        
        // Setup the stage
        primaryStage.setTitle("Competitions Admin Backoffice");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        
        // Initialize the controller
        BackofficeCompetitionsController controller = loader.getController();
        controller.loadCompetitions();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 