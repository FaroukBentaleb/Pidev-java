package tn.learniverse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the competitions list view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/backoffice_competitions.fxml"));
        Parent root = loader.load();
        
        // Setup the stage
        primaryStage.setTitle("Learniverse - Competitions");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 