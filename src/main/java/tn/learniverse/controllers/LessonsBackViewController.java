package tn.learniverse.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.learniverse.entities.Course;
import tn.learniverse.entities.Lesson;
import tn.learniverse.services.LessonService;

import java.sql.SQLException;
import java.util.List;

public class LessonsBackViewController {

    @FXML
    private Label courseTitleLabel;

    @FXML
    private TableView<Lesson> lessonTable;

    @FXML
    private TableColumn<Lesson, String> titleColumn;

    @FXML
    private TableColumn<Lesson, String> descriptionColumn;

    @FXML
    private TableColumn<Lesson, String> attachmentColumn;

    private final LessonService lessonService = new LessonService();

    public void setCourse(Course course) throws SQLException {
        courseTitleLabel.setText("Lessons for: " + course.getTitle());

        // Lier les colonnes
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        attachmentColumn.setCellValueFactory(new PropertyValueFactory<>("attachment"));

        // Charger les vraies données depuis la BDD
        List<Lesson> lessons = lessonService.getLessonsByCourse(course);
        lessonTable.setItems(FXCollections.observableArrayList(lessons));
    }
}
