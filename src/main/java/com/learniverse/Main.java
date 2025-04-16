package com.learniverse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/OffreView.fxml"));
        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
        primaryStage.setTitle("Learniverse - Offer Management");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);  // Optional: prevent window resizing
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 