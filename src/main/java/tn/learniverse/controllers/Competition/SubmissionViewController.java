package tn.learniverse.controllers.Competition;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import tn.learniverse.entities.Challenge;
import tn.learniverse.entities.Competition;
import tn.learniverse.entities.Submission;
import tn.learniverse.services.CompetitionService;
import tn.learniverse.services.GradingService;
import tn.learniverse.tools.*;
import tn.learniverse.tools.Session.User;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubmissionViewController implements Initializable {

    @FXML
    private Label competitionNameLabel;

    @FXML
    private Label countdownLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private VBox challengesContainer;

    @FXML
    private MFXButton submitButton;

    @FXML
    private MFXButton backButton;

    @FXML
    private StackPane loadingOverlay;

    @FXML
    private Circle loadingCircle;

    private Competition competition;
    private List<Challenge> challenges;
    private Map<Integer, Node> submissionFields = new HashMap<>();    private GradingService gradingService;
    private SimpleIntegerProperty remainingSeconds;
    private Timeline countdownTimeline;
    private LocalDateTime startTime;
    private boolean timerExpired = false;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set user name from session
        User currentUser = Session.getCurrentUser();
        submitButton.setOnAction(event -> handleSubmit(false));



        // Initialize the loading animation
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);
    }

    public void setCompetition(Competition competition) throws SQLException {
        this.competition = competition;
        this.gradingService = new GradingService(DatabaseConnection.getConnection());

        // Set competition name
        competitionNameLabel.setText(competition.getNom());

        // Initialize the countdown timer
        remainingSeconds = new SimpleIntegerProperty(competition.getDuration() * 60);
        initializeTimer();

        // Populate challenges
        this.challenges = competition.getChallenges(); // Ensure challenges are assigned

        populateChallenges();

        // Show ready confirmation
        Platform.runLater(this::showReadyConfirmation);
    }

    private void initializeTimer() {
        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    remainingSeconds.set(remainingSeconds.get() - 1);
                    updateTimerDisplay();
                })
        );
        countdownTimeline.setCycleCount(Animation.INDEFINITE);
    }

    private void updateTimerDisplay() {
        int seconds = remainingSeconds.get();

        if (seconds < 0) {
            if (!timerExpired) {
                timerExpired = true;
                showTimerExpiredWarning();
                Platform.runLater(() -> handleSubmit(true));
            }

            // Show negative time
            seconds = Math.abs(seconds);
            countdownLabel.getStyleClass().add("countdown-expired");
        } else if (seconds <= 60) {
            // Last minute
            countdownLabel.getStyleClass().remove("countdown-warning");
            countdownLabel.getStyleClass().add("countdown-danger");
        } else if (seconds <= 300) {
            // Last 5 minutes
            countdownLabel.getStyleClass().add("countdown-warning");
        }

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        countdownLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, secs));
    }

    private void populateChallenges() {
        challengesContainer.getChildren().clear();
        submissionFields.clear();

        for (Challenge challenge : competition.getChallenges()) {
            VBox challengeCard = new VBox();
            challengeCard.getStyleClass().add("challenge-card");
            challengeCard.setSpacing(15);

            // Challenge header
            HBox header = new HBox();
            header.getStyleClass().add("challenge-header");
            header.setPadding(new Insets(15));

            Label titleLabel = new Label(challenge.getTitle());
            titleLabel.getStyleClass().add("challenge-title");
            header.getChildren().add(titleLabel);

            // Challenge content
           VBox contentBox = new VBox();
            contentBox.getStyleClass().add("challenge-content");
            contentBox.setSpacing(10);

            String htmlContent = challenge.getContent();
            String finalHtml = CodeHighlighter.addSyntaxHighlighting(tn.learniverse.tools.HtmlDisplayer.createStyledHtml(htmlContent));
            WebView webView = new WebView();
            webView.setContextMenuEnabled(false);
            webView.getEngine().loadContent(finalHtml);
            webView.prefWidthProperty().bind(contentBox.widthProperty());
            webView.setPrefHeight(1); // Set a minimal height
            webView.getEngine().documentProperty().addListener((obs, oldDoc, newDoc) -> {
                if (newDoc != null) {
                    webView.getEngine().executeScript("document.body.setAttribute('contenteditable', false);");
                    Object result = webView.getEngine().executeScript("document.documentElement.scrollHeight");
                    if (result instanceof Number) {
                        double contentHeight = ((Number) result).doubleValue();
                        webView.setPrefHeight(contentHeight);
                    }
                }
            });
            contentBox.getChildren().add(webView);

            // Text area for submission
            CustomHtmlEditor submissionEditor = new CustomHtmlEditor(false, true);
            submissionEditor.setCustomPrefHeight(250);
            submissionEditor.setStyle("-fx-border-radius: 6px; -fx-background-radius: 6px; -fx-border-color: #D1DBE0;");
            // Store reference to text area
            submissionFields.put(challenge.getId(), submissionEditor);
            Platform.runLater(() -> {
                WebView innerWebView = (WebView) submissionEditor.lookup("WebView");
                if (innerWebView != null) {
                    innerWebView.getEngine().documentProperty().addListener((obs, oldDoc, newDoc) -> {
                        if (newDoc != null) {
                            innerWebView.getEngine().executeScript("" +
                                    "var placeholder = document.createElement('p');" +
                                    "placeholder.style.color = 'grey';" +
                                    "placeholder.textContent = 'Write your solution here...';" +
                                    "if (document.body.innerHTML === '') {" +
                                    "   document.body.appendChild(placeholder);" +
                                    "}" +
                                    "document.body.addEventListener('input', function() {" +
                                    "   if (document.body.textContent.trim() !== '') {" +
                                    "       placeholder.remove();" +
                                    "   } else if (!document.body.contains(placeholder)) {" +
                                    "       document.body.appendChild(placeholder);" +
                                    "   }" +
                                    "});");
                        }
                    });
                }
            });
            // Assemble the challenge card
challengeCard.getChildren().addAll(header, contentBox, submissionEditor);
challengesContainer.getChildren().add(challengeCard);
        }
    }

    private void showReadyConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Ready to Begin?");
        alert.setHeaderText("Start Competition");
        alert.setContentText("Once you start, the timer will begin. Are you prepared to tackle the challenges?");

        // Custom button types
        ButtonType startButton = new ButtonType("Let's Do It!");
        ButtonType cancelButton = new ButtonType("Not Yet");

        alert.getButtonTypes().setAll(startButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == startButton) {
            startTime = LocalDateTime.now();
            countdownTimeline.play();
        } else {
            handleBack(); // Go back if they're not ready
        }
    }


    private void showTimerExpiredWarning() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Time is Up!");
            alert.setHeaderText("Competition Time Expired");
            alert.setContentText("You have exceeded the time limit for this competition.");
            alert.showAndWait();
        });
    }


    @FXML
    private void handleSubmit(Boolean a) {
        Alert confirm = null;
        Optional<ButtonType> result;
        if(a==false) {
            // Show confirmation dialog
            confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Submit Solutions");
            confirm.setHeaderText("Are you sure?");
            confirm.setContentText("Once submitted, you cannot change your answers. Continue?");
            result = confirm.showAndWait();
        } else {
            result = Optional.of(ButtonType.OK);
        }
        if ((result.isPresent() && result.get() == ButtonType.OK)||a) {
            // Stop the timer
            if (countdownTimeline != null && countdownTimeline.getStatus() == Animation.Status.RUNNING) {
                countdownTimeline.stop();
            }

            // Calculate duration in minutes
            LocalDateTime endTime = LocalDateTime.now();
            long durationInSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
            int durationInMinutes = (int) Math.ceil(durationInSeconds / 60.0);

            // Collect submissions
            Map<Integer, String> submissions = new HashMap<>();
            for (Challenge challenge : challenges) {
                CustomHtmlEditor editor = (CustomHtmlEditor) submissionFields.get(challenge.getId());
                if (editor != null) {
                    submissions.put(challenge.getId(), editor.getHtmlText());
                }
            }

            // Show loading overlay
            loadingOverlay.setVisible(true);
            loadingOverlay.setManaged(true);
            loadingOverlay.setOpacity(1);

            // Submit in background
            executorService.submit(() -> {
                try {
                    User currentUser = Session.getCurrentUser();
                    if(!CompetitionService.isUserParticipant(competition.getId(), currentUser.getId())) {
                        CompetitionService.AddParticipant(competition);
                        CompetitionService.SetSubmmissionTrue(competition.getId(), currentUser.getId());
                    }
                    Map<Challenge, Submission> map=  gradingService.startCompetition(competition.getId(), currentUser, submissions, durationInMinutes);
                    System.out.println("map"+map);
                    // Navigate to results page on success
                    Platform.runLater(() -> {
                        loadingOverlay.setVisible(false);
                        loadingOverlay.setManaged(false);
                        navigateToResults(map,competition.getNom());
                    });
                } catch (SQLException | IOException | InterruptedException e) {
                    Platform.runLater(() -> {
                        loadingOverlay.setVisible(false);
                        loadingOverlay.setManaged(false);
                        showErrorAlert("Submission Error", "Failed to submit solutions: " + e.getMessage());
                    });
                    e.printStackTrace();
                }
            });
        }
    }

    @FXML
    private void handleBack() {
        // Show confirmation if timer is running
        if (countdownTimeline != null && countdownTimeline.getStatus() == Animation.Status.RUNNING) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Leave Competition?");
            alert.setHeaderText("Are you sure?");
            alert.setContentText("If you leave now, your progress will be lost.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                return;
            }

            // Stop the timer
            countdownTimeline.stop();
        }

        // Navigate back

    }

    private void navigateToResults(Map<Challenge, Submission>a,String z) {
        try {
            // Replace with your actual navigation code to results page
            NavigationManager.navigateToCompetitionResults(a,z);
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to navigate to results: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Clean up resources
    public void shutdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        executorService.shutdown();
    }
}