package tn.learniverse.controllers.Competition;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import tn.learniverse.entities.Challenge;
import tn.learniverse.entities.Competition;
import tn.learniverse.entities.Submission;
import tn.learniverse.tools.CodeHighlighter;
import tn.learniverse.tools.DatabaseConnection;
import tn.learniverse.tools.Session.User;
import tn.learniverse.services.CompetitionService;
import tn.learniverse.tools.Session;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static tn.learniverse.tools.RichTextUtils.extractTextFromHtml;

public class CompetitionLeaderboardController implements Initializable {

    @FXML
    private Label nameLabel;
    @FXML
    private Label competitionTitleLabel;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label startDateLabel;
    @FXML
    private Label endDateLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private ImageView competitionImage;
    @FXML
    private VBox overallLeaderboardContainer;
    @FXML
    private VBox challengeLeaderboardsContainer;
    @FXML
    private MFXButton overallMyRankButton;
    @FXML
    private TextField overallSearchField;
    @FXML
    private TitledPane challengeDetailsPane;
    @FXML
    private VBox challengeDetailsContent;


    @FXML
    private Label userInitialLabel;
    @FXML
    private Label userNameLabel;
    @FXML
    private MFXButton backButton;
    @FXML
    private MFXButton exportButton;

    // Top podium placeholders
    @FXML
    private Label firstPlaceInitial;
    @FXML
    private Label firstPlaceName;
    @FXML
    private Label firstPlaceScore;
    @FXML
    private Label secondPlaceInitial;
    @FXML
    private Label secondPlaceName;
    @FXML
    private Label secondPlaceScore;
    @FXML
    private Label thirdPlaceInitial;
    @FXML
    private Label thirdPlaceName;
    @FXML
    private Label thirdPlaceScore;

    private CompetitionService competitionService;
    private Competition competition;
    private int competitionId;
    private List<AbstractMap.SimpleEntry<User, Integer>> overallLeaderboardData;
    private Map<Challenge, List<AbstractMap.SimpleEntry<User, Integer>>> challengeLeaderboardsData;
    private Session session;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            competitionService = new CompetitionService(connection);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to connect to the database", e.getMessage());
            e.printStackTrace();
            return;
        }
        session = Session.getInstance();


        // Set user profile
        User currentUser = Session.getCurrentUser();

    }

    private void showAlert(Alert.AlertType alertType, String databaseError, String s, String message) {
    }

    public void initData(int competitionId) throws SQLException {
        this.competitionId = competitionId;
        loadCompetitionData();
        loadLeaderboardData();
    }

    private void loadCompetitionData() throws SQLException {
        competition = competitionService.getCompetitionById(competitionId);

        if (competition != null) {
            // Set competition details
            nameLabel.setText(competition.getNom());
            competitionTitleLabel.setText(competition.getNom());
            categoryLabel.setText(competition.getCategorie());
            String descriptionText = extractTextFromHtml(competition.getDescription());
            descriptionLabel.setText(descriptionText);
            descriptionLabel.setText(descriptionText);

            // Format dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
            startDateLabel.setText(competition.getDateComp().format(formatter));
            endDateLabel.setText(competition.getDateFin().format(formatter));

            // Set status with proper styling
            String status = competition.getEtat();
            statusLabel.setText(status);

            switch (status.toLowerCase()) {
                case "completed":

                    statusLabel.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 14px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 1);");
                    break;
                default:
                    statusLabel.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
            }

            // Load competition image
            if (competition.getWebImageUrl() != null && !competition.getWebImageUrl().isEmpty()) {
                try {
                    // Try to load from web URL first
                    Image image = new Image(competition.getWebImageUrl());
                    if (image.isError()) {
                        // Fall back to local file if web URL fails
                        loadLocalImage();
                    } else {
                        competitionImage.setImage(image);
                    }
                } catch (Exception e) {
                    loadLocalImage();
                }
            } else {
                loadLocalImage();
            }
        }
    }

    private void loadLocalImage() {
        // Check for local file path
        if (competition.getImageUrl() != null && !competition.getImageUrl().isEmpty()) {
            try {
                Image image = new Image("file:" + competition.getImageUrl());
                if (image.isError()) {
                    setDefaultImage();
                } else {
                    competitionImage.setImage(image);
                }
            } catch (Exception e) {
                setDefaultImage();
            }
        } else {
            setDefaultImage();
        }
    }

    private void setDefaultImage() {
    }


    private void loadLeaderboardData() {
        // Load overall leaderboard
        overallLeaderboardData = competitionService.getLeaderboard(competitionId);

        // Load challenge-specific leaderboards
        challengeLeaderboardsData = competitionService.getChallengeLeaderboards(competitionId);

        // Populate the UI with the data
        populateOverallLeaderboard();
        populateChallengeLeaderboards();

        // Check if the current user has submissions to enable/disable "Show My Rank" button
        checkUserParticipation();
    }

    private void checkUserParticipation() {
        User currentUser = session.getCurrentUser();
        if (currentUser != null) {
            int userId = currentUser.getId();

            // Check overall participation
            boolean participatedOverall = overallLeaderboardData.stream()
                    .anyMatch(entry -> entry.getKey().getId() == userId);

            overallMyRankButton.setDisable(!participatedOverall);

            if (!participatedOverall) {
                overallMyRankButton.setText("Not Participated");
                overallMyRankButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 15;");
            }
        }
    }

    private void populateOverallLeaderboard() {
        // Clear the container first
        overallLeaderboardContainer.getChildren().clear();

        if (overallLeaderboardData.isEmpty()) {
            Label noDataLabel = new Label("No participants yet");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 20;");
            overallLeaderboardContainer.getChildren().add(noDataLabel);

            // Hide podium section
            firstPlaceName.getParent().getParent().setVisible(false);
            return;
        }

        // Show podium section
        firstPlaceName.getParent().getParent().setVisible(true);

        // Populate top 3 podium spots
        if (overallLeaderboardData.size() >= 1) {
            AbstractMap.SimpleEntry<User, Integer> first = overallLeaderboardData.get(0);
            firstPlaceInitial.setText(first.getKey().getNom().substring(0, 1).toUpperCase());
            firstPlaceName.setText(first.getKey().getNom() + " " + first.getKey().getPrenom());
            firstPlaceScore.setText(first.getValue() + " pts");
        }

        if (overallLeaderboardData.size() >= 2) {
            AbstractMap.SimpleEntry<User, Integer> second = overallLeaderboardData.get(1);
            secondPlaceInitial.setText(second.getKey().getNom().substring(0, 1).toUpperCase());
            secondPlaceName.setText(second.getKey().getNom() + " " + second.getKey().getPrenom());
            secondPlaceScore.setText(second.getValue() + " pts");
        } else {
            secondPlaceName.getParent().setVisible(false);
        }

        if (overallLeaderboardData.size() >= 3) {
            AbstractMap.SimpleEntry<User, Integer> third = overallLeaderboardData.get(2);
            thirdPlaceInitial.setText(third.getKey().getNom().substring(0, 1).toUpperCase());
            thirdPlaceName.setText(third.getKey().getNom() + " " + third.getKey().getPrenom());
            thirdPlaceScore.setText(third.getValue() + " pts");
        } else {
            thirdPlaceName.getParent().setVisible(false);
        }

        // Start from rank 4 for the table list (since top 3 are in the podium)
        int startRank = 4;
        int endRank = Math.min(overallLeaderboardData.size(), 20); // Limit to top 20

        // Add the rest to the table
        for (int i = 3; i < endRank; i++) {
            AbstractMap.SimpleEntry<User, Integer> entry = overallLeaderboardData.get(i);
            HBox row = createLeaderboardRow(i + 1, entry);
            overallLeaderboardContainer.getChildren().add(row);
        }
    }

    private HBox createLeaderboardRow(int rank, AbstractMap.SimpleEntry<User, Integer> entry) {
        User user = entry.getKey();
        Integer score = entry.getValue();

        HBox row = new HBox();
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        // Check if this is the current user's row
        boolean isCurrentUser = session.getCurrentUser() != null && user.getId() == session.getCurrentUser().getId();
        if (isCurrentUser) {
            row.setStyle(row.getStyle() + "; -fx-background-color: #e3f2fd; -fx-border-color: #2196F3;");
        }

        // Rank column
        Label rankLabel = new Label(String.valueOf(rank));
        rankLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-min-width: 60; -fx-max-width: 60;");

        // User column with avatar
        HBox userBox = new HBox();
        userBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        userBox.setSpacing(10);
        userBox.setMinWidth(250);
        userBox.setMaxWidth(250);

        StackPane avatarPane = new StackPane();
        Circle avatar = new Circle(18);
        avatar.setFill(javafx.scene.paint.Color.valueOf("#e2e8f0"));

        Label initialLabel = new Label(user.getNom().substring(0, 1).toUpperCase());
        initialLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        avatarPane.getChildren().addAll(avatar, initialLabel);

        Label nameLabel = new Label(user.getNom() + " " + user.getPrenom());
        nameLabel.setStyle("-fx-font-size: 14px;");

        userBox.getChildren().addAll(avatarPane, nameLabel);

        // Score column
        Label scoreLabel = new Label(score + " pts");
        scoreLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        HBox.setHgrow(scoreLabel, Priority.ALWAYS);

        row.getChildren().addAll(rankLabel, userBox, scoreLabel);
        return row;
    }

    private void populateChallengeLeaderboards() {
        // Clear the container first
        challengeLeaderboardsContainer.getChildren().clear();

        if (challengeLeaderboardsData.isEmpty()) {
            Label noDataLabel = new Label("No challenge data available");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 20;");
            challengeLeaderboardsContainer.getChildren().add(noDataLabel);
            return;
        }

        // Create a section for each challenge
        for (Map.Entry<Challenge, List<AbstractMap.SimpleEntry<User, Integer>>> entry : challengeLeaderboardsData.entrySet()) {
            Challenge challenge = entry.getKey();
            List<AbstractMap.SimpleEntry<User, Integer>> leaderboard = entry.getValue();

            VBox challengeSection = createChallengeLeaderboardSection(challenge, leaderboard);
            challengeLeaderboardsContainer.getChildren().add(challengeSection);
        }
    }

    private VBox createChallengeLeaderboardSection(Challenge challenge, List<AbstractMap.SimpleEntry<User, Integer>> leaderboard) {
        VBox section = new VBox();
        section.setSpacing(10);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3); -fx-padding: 20;");

        // Header with title, search and my rank button
        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Challenge title with icon
        Label titleLabel = new Label(challenge.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        FontIcon titleIcon = new FontIcon("mdi2l-lightbulb");
        titleIcon.setIconColor(Color.valueOf("#2196F3"));
        titleIcon.setIconSize(22);
        titleLabel.setGraphic(titleIcon);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search field for this challenge
       TextField searchField = new TextField();
       searchField.setPromptText("Search by name");
       searchField.setPrefWidth(200);
       searchField.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 20; -fx-text-fill: #1E293B; -fx-prompt-text-fill: #94A3B8;");

       // Make search field functional
       searchField.setOnKeyReleased(event -> {
           String searchText = searchField.getText().toLowerCase().trim();
           filterChallengeLeaderboard(section, challenge, leaderboard, searchText);
       });
        Region spacer2 = new Region();
        spacer2.setPrefWidth(10);

        // Show My Rank button
        MFXButton myRankButton = new MFXButton("Show My Rank");
        myRankButton.setStyle("-fx-background-color: #3F51B5; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 15;");

        FontIcon rankIcon = new FontIcon("mdi2c-crosshairs");
        rankIcon.setIconColor(Color.WHITE);
        rankIcon.setIconSize(16);
        myRankButton.setGraphic(rankIcon);

        // Check if current user has participated in this challenge
        User currentUser = session.getCurrentUser();
        boolean hasParticipated = false;

        if (currentUser != null) {
            hasParticipated = leaderboard.stream()
                    .anyMatch(entry -> entry.getKey().getId() == currentUser.getId());

            if (!hasParticipated) {
                myRankButton.setDisable(true);
                myRankButton.setText("Not Participated");
                myRankButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 15;");
            } else {
                // Set action to scroll to user's rank
                myRankButton.setOnAction(event -> showUserChallengeRank(section, challenge.getId(), currentUser.getId()));
            }
        }

        header.getChildren().addAll(titleLabel, spacer, searchField, spacer2, myRankButton);

        // Table header
        HBox tableHeader = new HBox();
        tableHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        tableHeader.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8; -fx-padding: 10;");

        Label rankHeaderLabel = new Label("Rank");
        rankHeaderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 60; -fx-max-width: 60;");

        Label participantHeaderLabel = new Label("Participant");
        participantHeaderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 250; -fx-max-width: 250;");

        Label scoreHeaderLabel = new Label("Score");
        scoreHeaderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");
        HBox.setHgrow(scoreHeaderLabel, Priority.ALWAYS);

        tableHeader.getChildren().addAll(rankHeaderLabel, participantHeaderLabel, scoreHeaderLabel);

        // Leaderboard entries container
        VBox entriesContainer = new VBox();
        entriesContainer.setSpacing(5);
        entriesContainer.setId("challenge-" + challenge.getId() + "-entries");

        // Add entries to the container
        if (leaderboard.isEmpty()) {
            Label noDataLabel = new Label("No submissions yet");
            noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-padding: 15;");
            entriesContainer.getChildren().add(noDataLabel);
        } else {
            for (int i = 0; i < leaderboard.size(); i++) {
                AbstractMap.SimpleEntry<User, Integer> entry = leaderboard.get(i);
                HBox row = createLeaderboardRow(i + 1, entry);
                entriesContainer.getChildren().add(row);
            }
        }

        // Create an embedded TitledPane for challenge details
        TitledPane detailsPane = new TitledPane();
        detailsPane.setText("Challenge Details");
        detailsPane.setAnimated(true);
        detailsPane.setExpanded(false);
        detailsPane.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Create the content for the TitledPane now
        VBox detailsContent = new VBox(15);
        detailsContent.setStyle("-fx-padding: 15;");

        // Challenge Description Section
        VBox descriptionBox = new VBox(5);
        Label descriptionLabel = new Label("Challenge Description");
        descriptionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        WebView descriptionWebView = new WebView();
        descriptionWebView.setContextMenuEnabled(false);
        String challengeDescription = tn.learniverse.tools.HtmlDisplayer.createStyledHtml(challenge.getContent());
        descriptionWebView.getEngine().loadContent(challengeDescription);
        descriptionWebView.setPrefHeight(200); // Set initial height
        descriptionWebView.getEngine().documentProperty().addListener((obs, oldDoc, newDoc) -> {
            if (newDoc != null) {
                Platform.runLater(() -> {
                    try {
                        Object result = descriptionWebView.getEngine().executeScript("document.documentElement.scrollHeight");
                        if (result instanceof Number) {
                            descriptionWebView.setPrefHeight(((Number) result).doubleValue() + 20); // Add some padding
                        }
                    } catch (Exception e) {
                        // Handle script exception
                        descriptionWebView.setPrefHeight(200);
                    }
                });
            }
        });

        descriptionBox.getChildren().addAll(descriptionLabel, descriptionWebView);

        // Separator
        Separator separator = new Separator();

        // Solution Section
        VBox solutionBox = new VBox(5);
        Label solutionLabel = new Label("Solution");
        solutionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        WebView solutionWebView = new WebView();
        solutionWebView.setContextMenuEnabled(false);
        String solutionContent = CodeHighlighter.addSyntaxHighlighting(
                tn.learniverse.tools.HtmlDisplayer.createStyledHtml(challenge.getSolution())
        );
        solutionWebView.getEngine().loadContent(solutionContent);
        solutionWebView.setPrefHeight(200); // Set initial height
        solutionWebView.getEngine().documentProperty().addListener((obs, oldDoc, newDoc) -> {
            if (newDoc != null) {
                Platform.runLater(() -> {
                    try {
                        Object result = solutionWebView.getEngine().executeScript("document.documentElement.scrollHeight");
                        if (result instanceof Number) {
                            solutionWebView.setPrefHeight(((Number) result).doubleValue() + 20); // Add some padding
                        }
                    } catch (Exception e) {
                        // Handle script exception
                        solutionWebView.setPrefHeight(200);
                    }
                });
            }
        });

        solutionBox.getChildren().addAll(solutionLabel, solutionWebView);

        // Add all sections to the content
        detailsContent.getChildren().addAll(descriptionBox, separator, solutionBox);

        // Set the content to the TitledPane
        detailsPane.setContent(detailsContent);

        // Toggle button to expand/collapse the details pane
//        MFXButton viewDetailsButton = new MFXButton("View Challenge Details");
//        viewDetailsButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 15;");
//        FontIcon detailsIcon = new FontIcon("mdi2i-information");
//        detailsIcon.setIconColor(Color.WHITE);
//        detailsIcon.setIconSize(16);
//        viewDetailsButton.setGraphic(detailsIcon);
//
//        // Toggle the details pane when the button is clicked
//        viewDetailsButton.setOnAction(event -> {
//            detailsPane.setExpanded(!detailsPane.isExpanded());
//        });

        // Add all components to the section
        section.getChildren().addAll(header, tableHeader, entriesContainer, detailsPane);

        return section;
    }    private void filterChallengeLeaderboard(VBox section, Challenge challenge, List<AbstractMap.SimpleEntry<User, Integer>> fullLeaderboard, String searchText) {
        // Find the entries container
        VBox entriesContainer = (VBox) section.lookup("#challenge-" + challenge.getId() + "-entries");
        if (entriesContainer == null) return;

        entriesContainer.getChildren().clear();

        if (searchText.isEmpty()) {
            // Show all entries
            for (int i = 0; i < fullLeaderboard.size(); i++) {
                AbstractMap.SimpleEntry<User, Integer> entry = fullLeaderboard.get(i);
                HBox row = createLeaderboardRow(i + 1, entry);
                entriesContainer.getChildren().add(row);
            }
        } else {
            // Filter and show matching entries
            List<AbstractMap.SimpleEntry<User, Integer>> filteredList = fullLeaderboard.stream()
                    .filter(entry -> (entry.getKey().getNom() + " " + entry.getKey().getPrenom())
                            .toLowerCase().contains(searchText))
                    .collect(Collectors.toList());

            if (filteredList.isEmpty()) {
                Label noMatchLabel = new Label("No matching participants found");
                noMatchLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-padding: 15;");
                entriesContainer.getChildren().add(noMatchLabel);
            } else {
                for (int i = 0; i < filteredList.size(); i++) {
                    // Keep original ranking
                    int originalRank = fullLeaderboard.indexOf(filteredList.get(i)) + 1;
                    HBox row = createLeaderboardRow(originalRank, filteredList.get(i));
                    entriesContainer.getChildren().add(row);
                }
            }
        }
    }

    @FXML
    private void handleOverallSearch() {
        String searchText = overallSearchField.getText().toLowerCase().trim();

        // Clear container
        overallLeaderboardContainer.getChildren().clear();

        if (searchText.isEmpty()) {
            // Show regular leaderboard
            populateOverallLeaderboard();
        } else {
            // Filter and show matching entries
            List<AbstractMap.SimpleEntry<User, Integer>> filteredList = overallLeaderboardData.stream()
                    .filter(entry -> (entry.getKey().getNom() + " " + entry.getKey().getPrenom())
                            .toLowerCase().contains(searchText))
                    .collect(Collectors.toList());

            if (filteredList.isEmpty()) {
                Label noMatchLabel = new Label("No matching participants found");
                noMatchLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-padding: 15;");
                overallLeaderboardContainer.getChildren().add(noMatchLabel);
            } else {
                for (int i = 0; i < filteredList.size(); i++) {
                    // Keep original ranking
                    int originalRank = overallLeaderboardData.indexOf(filteredList.get(i)) + 1;
                    HBox row = createLeaderboardRow(originalRank, filteredList.get(i));
                    overallLeaderboardContainer.getChildren().add(row);
                }
            }
        }
    }

    @FXML
    private void handleShowMyOverallRank() {
        User currentUser = session.getCurrentUser();
        if (currentUser == null) return;

        int userId = currentUser.getId();

        // Find user's entry in the leaderboard
        for (int i = 0; i < overallLeaderboardData.size(); i++) {
            if (overallLeaderboardData.get(i).getKey().getId() == userId) {
                // Highlight and scroll to user's entry
                int rank = i + 1;

                // If rank is in top 3, they're in the podium, so we need to add a special row
                if (rank <= 3) {
                    // Add a special row for the user
                    HBox userRow = createLeaderboardRow(rank, overallLeaderboardData.get(i));
                    userRow.setStyle(userRow.getStyle() + "; -fx-background-color: #e3f2fd; -fx-border-color: #2196F3; -fx-border-width: 2;");

                    // Add at the top with a label
                    VBox container = new VBox();
                    container.setSpacing(5);

                    Label yourRankLabel = new Label("Your Rank:");
                    yourRankLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

                    container.getChildren().addAll(yourRankLabel, userRow);

                    // Add to the beginning of the leaderboard list
                    overallLeaderboardContainer.getChildren().add(0, container);
                } else {
                    // Create a temporary copy of the children to avoid concurrent modification
                    List<javafx.scene.Node> children = new ArrayList<>(overallLeaderboardContainer.getChildren());

                    // Find the row index (adjusted for top 3 not being in the table)
                    int rowIndex = rank - 4;

                    if (rowIndex >= 0 && rowIndex < children.size()) {
                        // Highlight the row
                        HBox row = (HBox) children.get(rowIndex);
                        row.setStyle(row.getStyle() + "; -fx-background-color: #e3f2fd; -fx-border-color: #2196F3; -fx-border-width: 2;");

                        // Scroll to the row
                        Platform.runLater(() -> row.requestFocus());
                    }
                }
                break;
            }
        }
    }

    private void showUserChallengeRank(VBox challengeSection, int challengeId, int userId) {
        // Find the entries container within the challenge section
        VBox entriesContainer = (VBox) challengeSection.lookup("#challenge-" + challengeId + "-entries");
        if (entriesContainer == null) return;

        // Find the user's row
        for (javafx.scene.Node node : entriesContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;

                // Check if this is the user's row (second element is the user box)
                HBox userBox = (HBox) row.getChildren().get(1);
                Label nameLabel = (Label) userBox.getChildren().get(1);

                // Check if name matches current user
                String username = session.getCurrentUser().getNom() + " " + session.getCurrentUser().getPrenom();
                if (nameLabel.getText().equals(username)) {
                    // Highlight the row
                    row.setStyle(row.getStyle() + "; -fx-background-color: #e3f2fd; -fx-border-color: #2196F3; -fx-border-width: 2;");

                    // Scroll to the row
                    Platform.runLater(() -> row.requestFocus());
                    break;
                }
            }
        }
    }

    @FXML
    private void handleBackToCompetition() {
//        Navigator.navigateTo("CompetitionView.fxml", param -> {
//            CompetitionViewController controller = (CompetitionViewController) param;
//            controller.initData(competitionId);
//        });
    }

    @FXML
    private void handleExportLeaderboard() {
        // This method would handle exporting the leaderboard to a file (e.g., CSV or PDF)
        // For now, just show an alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Leaderboard");
        alert.setHeaderText("Export Feature");
        alert.setContentText("The export feature will be implemented in a future update.");
        alert.showAndWait();
    }
}