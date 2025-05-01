package tn.learniverse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.learniverse.services.schedular;
import tn.learniverse.tools.Session;
import tn.learniverse.tools.NavigationManager;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        NavigationManager.initialize(primaryStage);
//       new schedular();
        String userRole = Session.getCurrentUser().getRole(); // Assuming Session stores the current user

        // Determine the FXML file to load based on the role
        String fxmlPath;
        if ("Admin".equalsIgnoreCase(userRole)) {
            fxmlPath = "/fxml/backoffice_competitions.fxml"; // Admin interface
        } else if ("Student".equalsIgnoreCase(userRole) || "Instructor".equalsIgnoreCase(userRole)) {
            fxmlPath = "/fxml/competitions_list.fxml"; // Front interface
        } else {
            throw new IllegalStateException("Unknown user role: " + userRole);
        }

        // Load the FXML file
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = new Scene(root);

        // Set up the stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("Application");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}