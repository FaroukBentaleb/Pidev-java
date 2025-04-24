package tn.learniverse.controllers.Competition;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import  tn.learniverse.entities.Competition;
import  tn.learniverse.services.CompetitionService;
import  tn.learniverse.tools.DatabaseConnection;
import org.kordamp.ikonli.javafx.FontIcon;

public class BackofficeCompetitionsController implements Initializable {

    @FXML
    private TextField searchField;
    
    @FXML
    private TableView<Competition> competitionsTable;
    
    @FXML
    private TableColumn<Competition, String> nameColumn;
    
    @FXML
    private TableColumn<Competition, String> categoryColumn;
    
    @FXML
    private TableColumn<Competition, String> statusColumn;
    
    @FXML
    private TableColumn<Competition, LocalDateTime> startDateColumn;
    
    @FXML
    private TableColumn<Competition, LocalDateTime> endDateColumn;
    
    @FXML
    private TableColumn<Competition, Integer> participantsColumn;
    
    @FXML
    private TableColumn<Competition, Void> actionsColumn;
    
    private CompetitionService competitionService;
    private ObservableList<Competition> competitionsList = FXCollections.observableArrayList();
    private FilteredList<Competition> filteredCompetitions;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize competition service
        try {
            Connection connection = DatabaseConnection.getConnection();
            competitionService = new CompetitionService(connection);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Failed to connect to the database", e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Configure table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));
        
        // Format dates using the DateFormatter
        startDateColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getDateComp()));
        
        endDateColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getDateFin()));
        
        // Set cell factory to format the dates
        startDateColumn.setCellFactory(column -> new TableCell<Competition, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(dateFormatter));
                }
            }
        });
        
        endDateColumn.setCellFactory(column -> new TableCell<Competition, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(dateFormatter));
                }
            }
        });
        
        participantsColumn.setCellValueFactory(new PropertyValueFactory<>("currentParticipant"));
        
        // Configure the actions column with buttons
        setupActionsColumn();
        
        // Load competitions and set up filtered list
        loadCompetitions();
        
        // Set up search functionality
        setupSearch();
        
        // Apply table styling
        applyTableStyling();
    }
    
    private void applyTableStyling() {
        // Set row height
        competitionsTable.setFixedCellSize(45);
        
        // Set font size for table cells
        competitionsTable.setStyle("-fx-font-size: 14px;");
        
        // Set column header style
        for (TableColumn<Competition, ?> column : competitionsTable.getColumns()) {
            column.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        }
        
        // Set alternating row colors
        competitionsTable.setStyle(competitionsTable.getStyle() + 
            "-fx-background-color: white;" +
            "-fx-table-cell-border-color: transparent;" +
            "-fx-padding: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;" +
            "-fx-border-color: #e0e0e0;" +
            "-fx-border-width: 1;");
    }
    
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> {
            return new TableCell<Competition, Void>() {
                private final Button freezeButton = new Button();
                private final Button detailsButton = new Button("Details");
                private final HBox buttonsBox = new HBox(5);
                
                {
                    // Configure buttons styling once
                    freezeButton.setStyle("-fx-background-radius: 5; -fx-font-size: 11px; -fx-padding: 3 8;");
                    FontIcon icon = new FontIcon("mdi2s-snowflake");
                    icon.setIconSize(12);
                    freezeButton.setGraphic(icon);
                    
                    freezeButton.setOnAction(event -> {
                        Competition competition = getTableView().getItems().get(getIndex());
                        handleFreezeCompetition(competition);
                    });
                    
                    detailsButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 11px; -fx-padding: 3 8;");
                    FontIcon infoIcon = new FontIcon("mdi2i-information-outline");
                    infoIcon.setIconColor(javafx.scene.paint.Color.WHITE);
                    infoIcon.setIconSize(12);
                    detailsButton.setGraphic(infoIcon);
                    
                    detailsButton.setOnAction(event -> {
                        Competition competition = getTableView().getItems().get(getIndex());
                        showCompetitionDetails(competition);
                    });
                    
                    // Setup the container
                    buttonsBox.setAlignment(Pos.CENTER);
                    buttonsBox.getChildren().addAll(freezeButton, detailsButton);
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Competition competition = getTableView().getItems().get(getIndex());
                        updateFreezeButton(competition);
                        setGraphic(buttonsBox);
                    }
                }
                
                private void updateFreezeButton(Competition competition) {
                    if (competition.isFreesed()) {
                        freezeButton.setText("Unfreeze");
                        freezeButton.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 11px; -fx-padding: 3 8;");
                    } else {
                        freezeButton.setText("Freeze");
                        freezeButton.setStyle("-fx-background-color: #eab308; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 11px; -fx-padding: 3 8;");
                    }
                }
            };
        });
    }
    
    private void setupSearch() {
        // Create a filtered list wrapper around the main list
        filteredCompetitions = new FilteredList<>(competitionsList, p -> true);
        
        // Set up the filter predicate based on search text
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCompetitions.setPredicate(competition -> {
                // If filter text is empty, display all competitions
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                // Compare competition fields with filter text
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (competition.getNom().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches name
                } else if (competition.getCategorie().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches category
                } else if (competition.getEtat().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches status
                }
                return false; // Does not match
            });
        });
        
        // Bind the filtered list to the table
        competitionsTable.setItems(filteredCompetitions);
    }
    
    public void loadCompetitions() {
        try {
            // Clear existing data
            competitionsList.clear();
            
            // Load competitions from database (with fresh data)
            List<Competition> competitions = competitionService.getAllCompetitions();
            
            // Add to observable list
            competitionsList.addAll(competitions);
            
            // If table is not yet bound to filtered list, do it now
            if (competitionsTable.getItems() != filteredCompetitions) {
                competitionsTable.setItems(filteredCompetitions);
            }
            
            // Ensure the table visually updates
            competitionsTable.refresh();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load competitions", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleFreezeCompetition(Competition competition) {
        try {
            // Toggle freeze status
            boolean newState = !competition.isFreesed();
            
            // Show confirmation dialog
            String action = newState ? "freeze" : "unfreeze";
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Action");
            alert.setHeaderText("Confirm " + action);
            alert.setContentText("Are you sure you want to " + action + " the competition '" + competition.getNom() + "'?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Update competition status
                competition.setFreesed(newState);
                
                // Update in database
                competitionService.updateCompetitionWithChallenges(competition, competition.getChallenges());
                
                // Refresh the table to ensure UI is updated
                Platform.runLater(() -> {
                    competitionsTable.refresh();
                });
                
                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Competition " + (newState ? "frozen" : "unfrozen") + " successfully", null);
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update competition status", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showCompetitionDetails(Competition competition) {
        try {
            // Create a custom dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Competition Details");
            dialog.setHeaderText("Details for " + competition.getNom());
            
            // Create content
            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 20;");
            
            // Header info
            content.getChildren().add(createInfoRow("Name:", competition.getNom()));
            content.getChildren().add(createInfoRow("Category:", competition.getCategorie()));
            content.getChildren().add(createInfoRow("Status:", competition.getEtat()));
            content.getChildren().add(createInfoRow("Start Date:", competition.getDateComp().format(dateFormatter)));
            content.getChildren().add(createInfoRow("End Date:", competition.getDateFin().format(dateFormatter)));
            content.getChildren().add(createInfoRow("Participants:", String.valueOf(competition.getCurrentParticipant())));
            
            // Description
            content.getChildren().add(new Label("Description:"));
            Label descriptionText = new Label(extractTextFromHtml(competition.getDescription()));
            descriptionText.setWrapText(true);
            descriptionText.setStyle("-fx-padding: 5 0 15 0;");
            content.getChildren().add(descriptionText);
            
            // Image if available
            if (competition.getWebImageUrl() != null && !competition.getWebImageUrl().isEmpty()) {
                try {
                    ImageView imageView = new ImageView(new Image(competition.getWebImageUrl(), true));
                    imageView.setFitWidth(200);
                    imageView.setPreserveRatio(true);
                    imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 5);");
                    
                    VBox imageContainer = new VBox(5, new Label("Cover:"), imageView);
                    imageContainer.setAlignment(Pos.CENTER);
                    content.getChildren().add(imageContainer);
                } catch (Exception e) {
                    // Fallback to local path if web URL fails
                    if (competition.getImageUrl() != null && !competition.getImageUrl().isEmpty()) {
                        try {
                            ImageView imageView = new ImageView(new Image("file:" + competition.getImageUrl(), true));
                            imageView.setFitWidth(200);
                            imageView.setPreserveRatio(true);
                            imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 5);");
                            
                            VBox imageContainer = new VBox(5, new Label("Cover:"), imageView);
                            imageContainer.setAlignment(Pos.CENTER);
                            content.getChildren().add(imageContainer);
                        } catch (Exception ex) {
                            // If both image sources fail, just don't show an image
                        }
                    }
                }
            }
            
            // Set the dialog content
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setContent(content);
            
            // Add close button
            dialogPane.getButtonTypes().add(ButtonType.CLOSE);
            
            // Style the dialog
            dialogPane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Show the dialog
            dialog.showAndWait();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to show competition details", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private HBox createInfoRow(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-min-width: 100;");
        
        Label valueNode = new Label(value);
        
        HBox row = new HBox(10, labelNode, valueNode);
        row.setAlignment(Pos.CENTER_LEFT);
        
        return row;
    }
    
    private String extractTextFromHtml(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        // Simple HTML tag removal (a more robust solution would use a proper HTML parser)
        return html.replaceAll("<[^>]*>", " ")
                   .replaceAll("&nbsp;", " ")
                   .replaceAll("\\s+", " ")
                   .trim();
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 