package tn.learniverse.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.learniverse.entities.Course;
import tn.learniverse.entities.Lesson;
import tn.learniverse.services.LessonService;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AddLessonController implements Initializable {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextArea contentField;

    @FXML
    private TextField attachmentField;

    @FXML
    private Label titleErrorLabel;

    @FXML
    private Label descriptionErrorLabel;

    @FXML
    private Label contentErrorLabel;

    @FXML
    private Label attachmentErrorLabel;

    @FXML
    private Button addButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button browseButton;

    private Course selectedCourse;
    private LessonService lessonService;
    private Runnable refreshCallback;

    // Si on veut modifier une leçon existante
    private Lesson existingLesson;
    private boolean isEditMode = false;

    // Pour stocker le fichier sélectionné
    private File selectedFile;

    // Chemin où les fichiers seront sauvegardés (dans le répertoire www de WAMP)
    private final String UPLOAD_DIRECTORY = "C:/wamp64/www/learniverse/uploads/lessons/";

    // URL relative pour accéder aux fichiers depuis le navigateur
    private final String WEB_URL_PREFIX = "http://localhost/learniverse/uploads/lessons/";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lessonService = new LessonService();

        // Initialisation des validations
        setupValidations();

        // S'assurer que le répertoire de téléchargement existe
        createUploadDirectoryIfNotExists();
    }

    private void createUploadDirectoryIfNotExists() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create upload directory: " + e.getMessage());
        }
    }

    public void setCourse(Course course) {
        this.selectedCourse = course;
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    public void setLessonForEdit(Lesson lesson) {
        this.existingLesson = lesson;
        this.isEditMode = true;

        // Remplir les champs avec les données de la leçon
        titleField.setText(lesson.getTitle());
        descriptionField.setText(lesson.getDescription());
        contentField.setText(lesson.getContent());
        attachmentField.setText(lesson.getAttachment());

        // Changer le texte du bouton
        addButton.setText("Update");
    }

    private void setupValidations() {
        // Validation du titre
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                titleErrorLabel.setText("Title cannot be empty");
                titleErrorLabel.setVisible(true);
            } else {
                titleErrorLabel.setVisible(false);
            }
        });

        // Validation de la description
        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                descriptionErrorLabel.setText("Description cannot be empty");
                descriptionErrorLabel.setVisible(true);
            } else if (newValue.length() < 10) {
                descriptionErrorLabel.setText("Description should be at least 10 characters");
                descriptionErrorLabel.setVisible(true);
            } else {
                descriptionErrorLabel.setVisible(false);
            }
        });

        // Validation du contenu
        contentField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                contentErrorLabel.setText("Content cannot be empty");
                contentErrorLabel.setVisible(true);
            } else {
                contentErrorLabel.setVisible(false);
            }
        });

        // Validation de l'attachement
        attachmentField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                // L'attachement est optionnel, donc pas d'erreur si vide
                attachmentErrorLabel.setVisible(false);
            } else {
                // Vérifier l'extension du fichier
                validateAttachmentExtension(newValue);
            }
        });
    }

    private void validateAttachmentExtension(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            attachmentErrorLabel.setVisible(false);
            return;
        }

        String extension = getFileExtension(fileName);
        if (extension == null) {
            attachmentErrorLabel.setText("File must have an extension");
            attachmentErrorLabel.setVisible(true);
            return;
        }

        // Extensions autorisées
        String[] allowedExtensions = {"pdf", "doc", "docx", "jpg", "jpeg", "png", "txt", "ppt", "pptx"};
        boolean isValid = false;

        for (String ext : allowedExtensions) {
            if (extension.equalsIgnoreCase(ext)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            attachmentErrorLabel.setText("Only allowed extensions: pdf, doc, docx, jpg, jpeg, png, txt, ppt, pptx");
            attachmentErrorLabel.setVisible(true);
        } else {
            attachmentErrorLabel.setVisible(false);
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(lastDot + 1);
    }

    @FXML
    private void browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attachment");

        // Configurer les filtres d'extension
        FileChooser.ExtensionFilter docFilter = new FileChooser.ExtensionFilter(
                "Documents (*.pdf, *.doc, *.docx, *.txt, *.ppt, *.pptx)", "*.pdf", "*.doc", "*.docx", "*.txt", "*.ppt", "*.pptx");
        FileChooser.ExtensionFilter imgFilter = new FileChooser.ExtensionFilter(
                "Images (*.jpg, *.jpeg, *.png)", "*.jpg", "*.jpeg", "*.png");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter(
                "All Files", "*.*");

        fileChooser.getExtensionFilters().addAll(docFilter, imgFilter, allFilter);

        // Afficher la boîte de dialogue
        Stage stage = (Stage) titleField.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            attachmentField.setText(selectedFile.getName());
            validateAttachmentExtension(selectedFile.getName());
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validation du titre
        if (titleField.getText().trim().isEmpty()) {
            titleErrorLabel.setText("Title cannot be empty");
            titleErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validation de la description
        if (descriptionField.getText().trim().isEmpty()) {
            descriptionErrorLabel.setText("Description cannot be empty");
            descriptionErrorLabel.setVisible(true);
            isValid = false;
        } else if (descriptionField.getText().length() < 10) {
            descriptionErrorLabel.setText("Description should be at least 10 characters");
            descriptionErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validation du contenu
        if (contentField.getText().trim().isEmpty()) {
            contentErrorLabel.setText("Content cannot be empty");
            contentErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validation de l'attachement si un fichier est spécifié
        if (attachmentField.getText() != null && !attachmentField.getText().trim().isEmpty()) {
            String fileName = attachmentField.getText();
            validateAttachmentExtension(fileName);
            if (attachmentErrorLabel.isVisible()) {
                isValid = false;
            }
        }

        return isValid;
    }

    private String saveAttachmentFile() {
        if (selectedFile == null) {
            return attachmentField.getText(); // Retourner le nom existant en mode édition
        }

        try {
            // Générer un nom de fichier unique pour éviter les écrasements
            String uniqueFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
            Path targetPath = Paths.get(UPLOAD_DIRECTORY, uniqueFileName);

            // Copier le fichier vers le répertoire de destination
            Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Retourner l'URL relative pour accéder au fichier via le serveur web
            return uniqueFileName;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save attachment: " + e.getMessage());
            return null;
        }
    }

    @FXML
    private void addLesson() {
        if (!validateInputs()) {
            return;
        }

        try {
            // Sauvegarder le fichier d'attachement s'il existe
            String savedFileName = saveAttachmentFile();

            if (isEditMode && existingLesson != null) {
                // Mode édition
                existingLesson.setTitle(titleField.getText());
                existingLesson.setDescription(descriptionField.getText());
                existingLesson.setContent(contentField.getText());

                // Mettre à jour l'attachement seulement si un nouveau fichier a été sélectionné
                if (selectedFile != null) {
                    existingLesson.setAttachment(savedFileName);
                }

                lessonService.updateLesson(existingLesson);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Lesson updated successfully!");
            } else {
                // Mode ajout
                Lesson newLesson = new Lesson(
                        titleField.getText(),
                        descriptionField.getText(),
                        contentField.getText(),
                        savedFileName,
                        selectedCourse
                );

                lessonService.addLesson(newLesson);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Lesson added successfully!");
            }

            // Appeler le callback pour rafraîchir la liste des leçons
            if (refreshCallback != null) {
                refreshCallback.run();
            }

            // Fermer la fenêtre
            closeWindow();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save lesson: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier");
        File selectedFile = fileChooser.showOpenDialog(attachmentField.getScene().getWindow());

        if (selectedFile != null) {
            attachmentField.setText(selectedFile.getAbsolutePath());
        }
    }
}