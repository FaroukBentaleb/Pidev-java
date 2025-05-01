package tn.learniverse.tools;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.learniverse.controllers.Competition.CompetitionViewController;
import tn.learniverse.controllers.Competition.ResultViewController;
import tn.learniverse.entities.Challenge;
import tn.learniverse.entities.Submission;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class NavigationManager {
    private static Stage mainStage;

    public static void initialize(Stage stage) {
        mainStage = stage;
    }

    public static void navigateToCompetitionResults(Map<Challenge, Submission> a, String z) throws IOException {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(NavigationManager.class.getResource("/fxml/result.fxml"));

            // Use the custom constructor
            ResultViewController controller = new ResultViewController(a, z);
            loader.setController(controller);

            Parent root = loader.load();

            // Set the scene
            setScene(root);

    }
    private static void setScene(Parent root) {
        Scene scene = new Scene(root);
        mainStage.setScene(scene);
        mainStage.show();
    }
}