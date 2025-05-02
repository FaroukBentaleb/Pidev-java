package tn.learniverse.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import tn.learniverse.entities.Course;
import tn.learniverse.services.CourseService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class BackController implements Initializable {

    @FXML
    private TableView<Course> courseTable;

    @FXML
    private TableColumn<Course, String> titleColumn;

    @FXML
    private TableColumn<Course, String> descriptionColumn;

    @FXML
    private TableColumn<Course, String> levelColumn;

    @FXML
    private TableColumn<Course, String> categoryColumn;

    @FXML
    private TableColumn<Course, Void> actionColumn;

    private final CourseService courseService = new CourseService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Lier les colonnes aux propriétés
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Charger les cours
        try {
            courseTable.setItems(FXCollections.observableArrayList(courseService.getAllCourses()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Ajouter la colonne avec le bouton
        addButtonToTable();
    }

    private void addButtonToTable() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("View Lessons");

            {
                btn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-background-radius: 6;");
                btn.setOnAction(event -> {
                    Course course = getTableView().getItems().get(getIndex());
                    openLessonsView(course);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void openLessonsView(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LessonsBackView.fxml"));
            Parent lessonsRoot = loader.load(); // ✅ Parent = classe de base de tous les containers

            LessonsBackViewController controller = loader.getController();
            controller.setCourse(course);

            Stage stage = new Stage();
            stage.setTitle("Lessons for: " + course.getTitle());
            stage.setScene(new Scene(lessonsRoot));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
