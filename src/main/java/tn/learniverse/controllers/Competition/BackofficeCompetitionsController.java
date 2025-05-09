package tn.learniverse.controllers.Competition;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
import javafx.scene.Node;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.stage.Stage;
import tn.learniverse.entities.Competition;
import tn.learniverse.services.CompetitionService;
import tn.learniverse.tools.DatabaseConnection;
import org.kordamp.ikonli.javafx.FontIcon;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

public class BackofficeCompetitionsController implements Initializable {

    public Label navUsernameLabel;
    public Button Profilebtn;
    public Button logoutButton;
    public Circle circleProfile;
    public Label FirstLetter;
    @FXML private TextField searchField;
    @FXML private MFXButton allStatusButton;
    @FXML private MFXButton plannedStatusButton;
    @FXML private MFXButton inProgressStatusButton;
    @FXML private MFXButton completedStatusButton;
    @FXML private TableView<Competition> competitionsTable;
    @FXML private TableColumn<Competition, String> nameColumn;
    @FXML private TableColumn<Competition, String> categoryColumn;
    @FXML private TableColumn<Competition, String> statusColumn;
    @FXML private TableColumn<Competition, LocalDateTime> startDateColumn;
    @FXML private TableColumn<Competition, LocalDateTime> endDateColumn;
    @FXML private TableColumn<Competition, Integer> participantsColumn;
    @FXML private TableColumn<Competition, Void> actionsColumn;
    @FXML private HBox paginationContainer;
    @FXML private MFXButton prevButton;
    @FXML private MFXButton nextButton;
    @FXML private Label competitionCount;

    private CompetitionService competitionService;
    private ObservableList<Competition> competitionsList = FXCollections.observableArrayList();
    private FilteredList<Competition> filteredCompetitions;
    private ObservableList<Competition> paginatedCompetitions = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private int currentPage = 1;
    private final int itemsPerPage = 10;
    private int totalPages = 1;
    private static final int MAX_VISIBLE_PAGES = 5;
    private String selectedStatus = "All";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            ImageView imageView = new ImageView();
            Image image = new Image("file:///C:/wamp64/www/images/icon/logout.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.logoutButton.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        try {
            ImageView imageView = new ImageView();
            Image image = new Image("file:///C:/wamp64/www/images/icon/profile.png",
                    16, 16, true, true);
            if (image.isError()) {
                System.out.println("Error loading image: " + image.getException().getMessage());
            } else {
                imageView.setImage(image);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                imageView.setPreserveRatio(true);
                this.Profilebtn.setGraphic(imageView);
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        if(Session.getCurrentUser()!=null){
            this.navUsernameLabel.setText(Session.getCurrentUser().getNom());
            this.FirstLetter.setText(Session.getCurrentUser().getNom().toUpperCase().substring(0, 1));
            Random random = new Random();
            Color randomColor = Color.rgb(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
            );
            circleProfile.setFill(randomColor);
        }
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

        // Status column badge styling
        statusColumn.setCellFactory(column -> new TableCell<Competition, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setStyle("-fx-alignment: CENTER; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-font-weight: bold; " +
                            "-fx-background-radius: 12; -fx-padding: 5 10;");
                    switch (status) {
                        case "Planned":
                            setStyle(getStyle() + "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;");
                            break;
                        case "InProgress":
                            setStyle(getStyle() + "-fx-background-color: #bfdbfe; -fx-text-fill: #1e40af;");
                            break;
                        case "Completed":
                            setStyle(getStyle() + "-fx-background-color: #dcfce7; -fx-text-fill: #166534;");
                            break;
                    }
                }
            }
        });

        // Configure the actions column with buttons
        setupActionsColumn();

        // Set up search and filter functionality
        setupSearchAndFilter();

        // Initialize button styles
        updateButtonStyles();

        // Load competitions
        loadCompetitions();

        // Apply table styling
        applyTableStyling();
    }

    private void applyTableStyling() {
        // Set row height
        competitionsTable.setFixedCellSize(60);

        // Override default styling from FXML
        competitionsTable.setStyle("-fx-font-size: 14px; -fx-font-family: 'Inter';");
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> {
            return new TableCell<Competition, Void>() {
                private final Button freezeButton = new Button();
                private final Button detailsButton = new Button("Details");
                private final HBox buttonsBox = new HBox(8);

                {
                    // Configure buttons styling once
                    freezeButton.setStyle("-fx-background-radius: 8; -fx-font-size: 12px; -fx-padding: 8 14; -fx-font-family: 'Inter';");
                    FontIcon icon = new FontIcon("mdi2s-snowflake");
                    icon.setIconSize(16);
                    freezeButton.setGraphic(icon);

                    freezeButton.setOnAction(event -> {
                        Competition competition = getTableView().getItems().get(getIndex());
                        handleFreezeCompetition(competition);
                    });

                    detailsButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 12px; -fx-padding: 8 14; -fx-font-family: 'Inter';");
                    FontIcon infoIcon = new FontIcon("mdi2i-information-outline");
                    infoIcon.setIconColor(javafx.scene.paint.Color.WHITE);
                    infoIcon.setIconSize(16);
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
                        freezeButton.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 12px; -fx-padding: 8 14; -fx-font-family: 'Inter';");
                    } else {
                        freezeButton.setText("Freeze");
                        freezeButton.setStyle("-fx-background-color: #eab308; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 12px; -fx-padding: 8 14; -fx-font-family: 'Inter';");
                    }
                }
            };
        });
    }

    private void setupSearchAndFilter() {
        // Initialize filtered list
        filteredCompetitions = new FilteredList<>(competitionsList, p -> true);

        // Set up search predicate
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentPage = 1;
            updateTableData();
        });

        // Bind filtered list to table
        competitionsTable.setItems(paginatedCompetitions);
    }

    @FXML
    private void handleStatusFilter(ActionEvent event) {
        MFXButton source = (MFXButton) event.getSource();
        selectedStatus = source.getText();
        currentPage = 1;
        updateButtonStyles();
        updateTableData();
    }

    private void updateButtonStyles() {
        // Reset all buttons to default style
        allStatusButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 10 20; -fx-font-family: 'Inter'; -fx-font-size: 14px;");
        plannedStatusButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 10 20; -fx-font-family: 'Inter'; -fx-font-size: 14px;");
        inProgressStatusButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 10 20; -fx-font-family: 'Inter'; -fx-font-size: 14px;");
        completedStatusButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e293b; -fx-background-radius: 25; -fx-padding: 10 20; -fx-font-family: 'Inter'; -fx-font-size: 14px;");

        // Highlight selected button
        MFXButton selectedButton = switch (selectedStatus) {
            case "Planned" -> plannedStatusButton;
            case "InProgress" -> inProgressStatusButton;
            case "Completed" -> completedStatusButton;
            default -> allStatusButton;
        };
        selectedButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 10 20; -fx-font-family: 'Inter'; -fx-font-size: 14px;");
    }

    public void loadCompetitions() {
        try {
            // Clear existing data
            competitionsList.clear();

            // Load competitions from database
            List<Competition> competitions = competitionService.getAllCompetitions();

            // Add to observable list
            competitionsList.addAll(competitions);

            // Update table data
            updateTableData();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load competitions", e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTableData() {
        // Filter competitions
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();

        filteredCompetitions.setPredicate(competition -> {
            boolean searchMatch = searchText.isEmpty() ||
                    competition.getNom().toLowerCase().contains(searchText) ||
                    competition.getCategorie().toLowerCase().contains(searchText) ||
                    competition.getEtat().toLowerCase().contains(searchText);

            boolean statusMatch = selectedStatus.equals("All") ||
                    competition.getEtat().equals(selectedStatus);

            return searchMatch && statusMatch;
        });

        // Update total pages
        totalPages = (int) Math.ceil((double) filteredCompetitions.size() / itemsPerPage);
        currentPage = Math.min(currentPage, totalPages);
        if (currentPage < 1) currentPage = 1;

        // Update paginated data
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredCompetitions.size());
        paginatedCompetitions.clear();
        paginatedCompetitions.addAll(filteredCompetitions.subList(startIndex, endIndex));

        // Update competition count
        competitionCount.setText("(" + filteredCompetitions.size() + ")");

        // Refresh table
        competitionsTable.refresh();

        // Update pagination
        updatePagination();
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updateTableData();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updateTableData();
        }
    }

    private void updatePagination() {
        paginationContainer.getChildren().clear();
        paginationContainer.getChildren().add(prevButton);

        int startPage = Math.max(1, currentPage - MAX_VISIBLE_PAGES / 2);
        int endPage = Math.min(totalPages, startPage + MAX_VISIBLE_PAGES - 1);

        if (startPage > 1) {
            addPageButton(1);
            if (startPage > 2) {
                addEllipsis();
            }
        }

        for (int i = startPage; i <= endPage; i++) {
            addPageButton(i);
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                addEllipsis();
            }
            addPageButton(totalPages);
        }

        paginationContainer.getChildren().add(nextButton);

        prevButton.setDisable(currentPage == 1);
        nextButton.setDisable(currentPage == totalPages);
    }

    private void addPageButton(int page) {
        MFXButton pageButton = new MFXButton(String.valueOf(page));
        pageButton.getStyleClass().addAll("mfx-button", "pagination-button");
        pageButton.setStyle("-fx-background-color: " + (page == currentPage ? "#2563eb" : "#ffffff") +
                "; -fx-text-fill: " + (page == currentPage ? "white" : "#1e293b") +
                "; -fx-background-radius: 10; -fx-padding: 10 16; -fx-font-size: 14px; -fx-font-family: 'Inter'; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");
        if (page == currentPage) {
            pageButton.getStyleClass().add("active");
        }
        pageButton.setOnAction(e -> {
            currentPage = page;
            updateTableData();
        });
        paginationContainer.getChildren().add(pageButton);
    }

    private void addEllipsis() {
        Text ellipsis = new Text("...");
        ellipsis.setStyle("-fx-font-size: 14px; -fx-fill: #1e293b; -fx-font-family: 'Inter';");
        paginationContainer.getChildren().add(ellipsis);
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
        labelNode.setStyle("-fx-font-weight: bold; -fx-min-width: 100; -fx-font-family: 'Inter';");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-family: 'Inter';");

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
                .replaceAll(" ", " ")
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

    @FXML
    private void handleStatistics(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/backoffice_statistics.fxml"));
        Parent view = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(view));
        stage.show();
    }

    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.redirect(actionEvent,"/fxml/user/login.fxml");
    }

    public void usersButton(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/usersBack.fxml");
    }

    public void Profile(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/ProfileBack.fxml");
    }
    public void Comp(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/backoffice_competitions.fxml");
    }

    public void ToReclamations(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/Reclamation/DisplayReclamationBack.fxml");
    }

    public void ToForums(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/BackForum.fxml");
    }

    public void ToCourses(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/BackCourses.fxml");
    }

    public void ToOffers(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/BackCourses.fxml");
    }

    public void ToDash(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/Back.fxml");
    }
}