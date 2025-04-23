package tn.learniverse.controllers.Reclamation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.learniverse.entities.Reclamation;
import tn.learniverse.entities.User;
import tn.learniverse.services.ReclamationService;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AjouterReclamation implements Initializable {

    public Button btnUpload;
    @FXML
    private Button ReclamationAjouter;

    @FXML
    private TextArea reclamationContenu;

    @FXML
    private ImageView reclamationImg;

    @FXML
    private TextField reclamationtTitre;

    @FXML
    private TextField filePathTextField;

    @FXML
    private Label titreErrorLabel;

    @FXML
    private Label contenuErrorLabel;

    @FXML
    private Label fichierErrorLabel;

    private ReclamationService reclamationService = new ReclamationService();
    private DisplayReclamations displayReclamationsController;

    private final Pattern LETTERS_ONLY_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s]+$");
    private final Pattern ALLOWED_FILE_PATTERN = Pattern.compile(".*\\.(pdf|jpg|jpeg|png)$", Pattern.CASE_INSENSITIVE);

    private String fullFilePath;

    public void setDisplayReclamationsController(DisplayReclamations controller) {
        this.displayReclamationsController = controller;
    }

    @FXML
    public void ajouterReclamation(ActionEvent event) {
        resetErrorMessages();

        boolean isValid = validateAllFields();

        if (isValid) {
            String titre = reclamationtTitre.getText();
            String contenu = reclamationContenu.getText();
            String fichier = this.fullFilePath != null ? this.fullFilePath : "";

            Reclamation reclamation = new Reclamation();
            reclamation.setTitre(titre);
            reclamation.setContenu(contenu);
            reclamation.setFichier(fichier);

            User user = new User();
            user.setId(3);

            try {
                reclamationService.ajouter(reclamation, user);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Réclamation ajoutée avec succès !");
                alert.showAndWait();

                Stage currentStage = (Stage) ReclamationAjouter.getScene().getWindow();
                currentStage.close();

                if (displayReclamationsController != null) {
                    displayReclamationsController.initialize();
                }
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de l'ajout de la réclamation: " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }

    private boolean validateAllFields() {
        boolean isTitreValid = validateTitre();
        boolean isContenuValid = validateContenu();
        boolean isFichierValid = validateFichier();

        return isTitreValid && isContenuValid && isFichierValid;
    }

    private boolean validateTitre() {
        String titre = reclamationtTitre.getText().trim();

        if (titre.isEmpty()) {
            titreErrorLabel.setText("Le titre est obligatoire");
            titreErrorLabel.setTextFill(Color.RED);
            reclamationtTitre.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
            return false;
        } else if (titre.length() < 3) {
            titreErrorLabel.setText("Le titre doit contenir au moins 3 caractères");
            titreErrorLabel.setTextFill(Color.RED);
            reclamationtTitre.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
            return false;
        } else if (!LETTERS_ONLY_PATTERN.matcher(titre).matches()) {
            titreErrorLabel.setText("Le titre ne doit contenir que des lettres");
            titreErrorLabel.setTextFill(Color.RED);
            reclamationtTitre.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
            return false;
        }

        titreErrorLabel.setText("");
        return true;
    }

    private boolean validateContenu() {
        String contenu = reclamationContenu.getText().trim();

        if (contenu.isEmpty()) {
            contenuErrorLabel.setText("Le contenu est obligatoire");
            contenuErrorLabel.setTextFill(Color.RED);
            reclamationContenu.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
            return false;
        } else if (contenu.length() < 10) {
            contenuErrorLabel.setText("Le contenu doit contenir au moins 10 caractères");
            contenuErrorLabel.setTextFill(Color.RED);
            reclamationContenu.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
            return false;
        }

        contenuErrorLabel.setText("");
        return true;
    }

    private boolean validateFichier() {
        String fileName = filePathTextField.getText().trim();

        if (!fileName.isEmpty()) {
            if (!ALLOWED_FILE_PATTERN.matcher(fileName).matches()) {
                fichierErrorLabel.setText("Format de fichier non accepté. Utilisez PDF, JPG ou PNG uniquement.");
                fichierErrorLabel.setTextFill(Color.RED);
                filePathTextField.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
                return false;
            }

            if (this.fullFilePath != null) {
                File file = new File(this.fullFilePath);
                if (!file.exists()) {
                    fichierErrorLabel.setText("Le fichier n'existe pas.");
                    fichierErrorLabel.setTextFill(Color.RED);
                    filePathTextField.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
                    return false;
                }
            }
        }

        return true;
    }

    private void resetErrorMessages() {
        titreErrorLabel.setText("");
        contenuErrorLabel.setText("");
        fichierErrorLabel.setText("");
    }

    @FXML
    private void handleJoinFile() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir un fichier");

            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Fichiers acceptés", "*.pdf", "*.jpg", "*.jpeg", "*.png")
            );

            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                String fileName = selectedFile.getName().toLowerCase();
                if (!ALLOWED_FILE_PATTERN.matcher(fileName).matches()) {
                    fichierErrorLabel.setTextFill(Color.RED);
                    fichierErrorLabel.setText("Format de fichier non accepté. Utilisez PDF, JPG ou PNG uniquement.");
                    return;
                }

                try {
                    File uploadDir = new File("C:/wamp64/www/images/reclamation");
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs();
                    }
                    String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                    String newFileName = "reclamation_" + System.currentTimeMillis() + extension;
                    String destinationPath = "C:/wamp64/www/images/reclamation/" + newFileName;
                    File destinationFile = new File(destinationPath);
                    Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    this.fullFilePath = destinationPath;
                    filePathTextField.setText(newFileName);

                    if (extension.toLowerCase().matches("\\.(png|jpg|jpeg)$")) {
                        Image image = new Image(destinationFile.toURI().toString());
                        reclamationImg.setImage(image);
                    } else if (extension.toLowerCase().equals(".pdf")) {
                        reclamationImg.setImage(new Image("file:///C:/wamp64/www/images/icons/pdf_icon.png"));
                    }

                    fichierErrorLabel.setTextFill(Color.GREEN);
                    fichierErrorLabel.setText("Fichier uploadé avec succès !");
                    filePathTextField.setStyle("-fx-border-color: green; -fx-border-radius: 8;");

                } catch (IOException e) {
                    fichierErrorLabel.setTextFill(Color.RED);
                    fichierErrorLabel.setText("Erreur lors de l'upload: " + e.getMessage());
                    filePathTextField.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            fichierErrorLabel.setTextFill(Color.RED);
            fichierErrorLabel.setText("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.reclamationImg.setImage(new Image("file:///C:/wamp64/www/images/logo/logo.png"));

        setupValidationListeners();
    }

    private void setupValidationListeners() {
        reclamationtTitre.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                reclamationtTitre.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
                titreErrorLabel.setText("");
            } else if (newValue.trim().length() < 3) {
                reclamationtTitre.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
                titreErrorLabel.setText("Le titre doit contenir au moins 3 caractères");
                titreErrorLabel.setTextFill(Color.RED);
            } else if (!LETTERS_ONLY_PATTERN.matcher(newValue.trim()).matches()) {
                reclamationtTitre.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
                titreErrorLabel.setText("Le titre ne doit contenir que des lettres");
                titreErrorLabel.setTextFill(Color.RED);
            } else {
                reclamationtTitre.setStyle("-fx-border-color: green; -fx-border-radius: 8;");
                titreErrorLabel.setText("");
            }
        });
        reclamationContenu.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                reclamationContenu.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
                contenuErrorLabel.setText("");
            } else if (newValue.trim().length() < 10) {
                reclamationContenu.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
                contenuErrorLabel.setText("Le contenu doit contenir au moins 10 caractères");
                contenuErrorLabel.setTextFill(Color.RED);
            } else {
                reclamationContenu.setStyle("-fx-border-color: green; -fx-border-radius: 8;");
                contenuErrorLabel.setText("");
            }
        });

        filePathTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                filePathTextField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
                fichierErrorLabel.setText("");
            } else {
                if (!ALLOWED_FILE_PATTERN.matcher(newValue).matches()) {
                    filePathTextField.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
                    fichierErrorLabel.setText("Format de fichier non accepté. Utilisez PDF, JPG ou PNG uniquement.");
                    fichierErrorLabel.setTextFill(Color.RED);
                } else if (this.fullFilePath != null) {
                    File file = new File(this.fullFilePath);
                    if (!file.exists()) {
                        filePathTextField.setStyle("-fx-border-color: red; -fx-border-radius: 8;");
                        fichierErrorLabel.setText("Le fichier n'existe pas.");
                        fichierErrorLabel.setTextFill(Color.RED);
                    } else {
                        filePathTextField.setStyle("-fx-border-color: green; -fx-border-radius: 8;");
                        fichierErrorLabel.setText("");
                    }
                }
            }
        });
    }
}