package tn.learniverse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OffreDisplay.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
        primaryStage.setTitle("Learniverse - Offer Management");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);  // Allow window resizing for better responsiveness
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 