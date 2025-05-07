package tn.learniverse.controllers.Competition;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;
import tn.learniverse.entities.Challenge;
import tn.learniverse.entities.Submission;
import tn.learniverse.tools.CodeHighlighter;
import tn.learniverse.tools.HtmlDisplayer;
import tn.learniverse.tools.Navigator;
import tn.learniverse.tools.Session;

public class ResultViewController implements Initializable {

    @FXML
    private Label competitionNameLabel;

    @FXML
    private Label totalScoreLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label completionTimeLabel;

    @FXML
    private Label submissionDateLabel;

    @FXML
    private VBox resultsContainer;

    @FXML
    private MFXButton backButton;

    @FXML
    private MFXButton exportButton;

    @FXML
    private MFXButton shareButton;
    public ImageView UserPicture;


    private String competitionName;
    private Map<Challenge, Submission> challengeSubmissionMap;
    private int totalScore = 0;
    private int maxPossibleScore = 0;

    public ResultViewController(Map<Challenge, Submission> challengeSubmissionMap, String competitionName) {
        this.challengeSubmissionMap = challengeSubmissionMap;
        this.competitionName = competitionName;
    }
    public ResultViewController() {
        // Default constructor for FXML
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        System.out.println("ChallengeSubmissionMap contents:");
        challengeSubmissionMap.forEach((challenge, submission) -> {
            System.out.println("Challenge: " + challenge.getTitle() + ", Submission Rating: " + submission.getRating());
        });
        competitionNameLabel.setText(competitionName);

        Image image = null;
//        image=new Image("file:///C:/wamp64/www/images/users/user.jpg", 50, 50, false, false);

        this.UserPicture.setImage(image);
        Circle clip = new Circle(25, 25, 25);
        this.UserPicture.setClip(clip);
        // Set username from the first submission
        if (!challengeSubmissionMap.isEmpty()) {
            Submission firstSubmission = challengeSubmissionMap.values().iterator().next();

            // Set completion time and submission date for the first submission
            completionTimeLabel.setText(formatMinutes(firstSubmission.getTimeTaken()));
            submissionDateLabel.setText(formatDate(firstSubmission.getDate()));
        }

        // Display results
        displayResults();

        // Set up button handlers
        backButton.setOnAction(event -> handleBack());
        exportButton.setOnAction(event -> handleExport());
    }

    private void displayResults() {
        resultsContainer.getChildren().clear();
        totalScore = 0;
        maxPossibleScore = challengeSubmissionMap.size() * 20; // Each submission can have max 20 points

        for (Map.Entry<Challenge, Submission> entry : challengeSubmissionMap.entrySet()) {
            Challenge challenge = entry.getKey();
            Submission submission = entry.getValue();

            // Add to total score
            totalScore += submission.getRating();

            // Create result card
            VBox resultCard = createResultCard(challenge, submission);
            resultsContainer.getChildren().add(resultCard);
        }

        // Update total score label
        totalScoreLabel.setText(totalScore + "/" + maxPossibleScore);
    }

    private VBox createResultCard(Challenge challenge, Submission submission) {
        VBox card = new VBox();
        card.getStyleClass().add("result-card");
        card.setSpacing(15);

        // Challenge header
        HBox header = new HBox();
        header.getStyleClass().add("result-header");
        header.setPadding(new Insets(15));

        Label titleLabel = new Label(challenge.getTitle());
        titleLabel.getStyleClass().add("result-title");
        header.getChildren().add(titleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().add(spacer);

        // Add rating badge
        int rating = submission.getRating();
        String ratingClass = "rating-high";
        if (rating <= 7) {
            ratingClass = "rating-low";
        } else if (rating <= 14) {
            ratingClass = "rating-medium";
        }

        Label ratingLabel = new Label(rating + " / 20");
        ratingLabel.getStyleClass().add(ratingClass);
        header.getChildren().add(ratingLabel);

        // Challenge content section
       VBox contentBox = new VBox();
                contentBox.getStyleClass().add("result-content");
                contentBox.setSpacing(20);

                // Challenge description
                VBox challengeDescBox = new VBox();
                challengeDescBox.setSpacing(10);
                Label challengeDescLabel = new Label("Challenge Description:");
                challengeDescLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

                WebView challengeWebView = new WebView();
                challengeWebView.setContextMenuEnabled(false);
                String challengeContent = challenge.getContent();
                String finalHtmml = CodeHighlighter.addSyntaxHighlighting(HtmlDisplayer.createStyledHtml(challengeContent));
                challengeWebView.getEngine().loadContent(finalHtmml);
                challengeWebView.prefWidthProperty().bind(contentBox.widthProperty());
                challengeWebView.setPrefHeight(1);
                challengeWebView.getEngine().documentProperty().addListener((obs, oldDoc, newDoc) -> {
                    if (newDoc != null) {
                        challengeWebView.getEngine().executeScript("document.body.setAttribute('contenteditable', false);");
                        Object result = challengeWebView.getEngine().executeScript("document.documentElement.scrollHeight");
                        if (result instanceof Number) {
                            double contentHeight = ((Number) result).doubleValue();
                            challengeWebView.setPrefHeight(contentHeight);
                        }
                    }
                });

                challengeDescBox.getChildren().addAll(challengeDescLabel, challengeWebView);

        // User solution section
        // User solution section
                VBox solutionBox = new VBox();
                solutionBox.setSpacing(10);
                solutionBox.getStyleClass().add("code-section");
                Label solutionLabel = new Label("Your Solution:");
                solutionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

                WebView solutionWebView = new WebView();
                solutionWebView.setContextMenuEnabled(false);
                String studentTry = submission.getStudentTry();
                String finalHtmll = CodeHighlighter.addSyntaxHighlighting(HtmlDisplayer.createStyledHtml(studentTry));
                solutionWebView.getEngine().loadContent(finalHtmll);
                solutionWebView.prefWidthProperty().bind(contentBox.widthProperty());
                solutionWebView.setPrefHeight(1);
                solutionWebView.getEngine().documentProperty().addListener((obs, oldDoc, newDoc) -> {
                    if (newDoc != null) {
                        solutionWebView.getEngine().executeScript("document.body.setAttribute('contenteditable', false);");
                        Object result = solutionWebView.getEngine().executeScript("document.documentElement.scrollHeight");
                        if (result instanceof Number) {
                            double contentHeight = ((Number) result).doubleValue();
                            solutionWebView.setPrefHeight(contentHeight);
                        }
                    }
                });

                solutionBox.getChildren().addAll(solutionLabel, solutionWebView);

        // AI Feedback section
        VBox feedbackBox = new VBox();
        feedbackBox.setSpacing(10);
        feedbackBox.getStyleClass().add("feedback-section");
        Label feedbackLabel = new Label("AI Feedback:");
        feedbackLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Use WebView to display HTML formatting in the feedback
        WebView feedbackWebView = new WebView();
        feedbackWebView.setPrefHeight(150);
        feedbackWebView.getEngine().loadContent(
                "<html><head><style>body{font-family:Arial,sans-serif;font-size:14px;padding:10px;margin:0;}</style></head>" +
                        "<body>" + (submission.getAiFeedback() != null && submission.getAiFeedback().length > 0 ?
                        submission.getAiFeedback()[0] : "No feedback available") + "</body></html>"
        );

        feedbackBox.getChildren().addAll(feedbackLabel, feedbackWebView);

        // Corrected solution section (if available)
        if (submission.getCorrectedCode() != null && submission.getCorrectedCode().length > 0 &&
                submission.getCorrectedCode()[0] != null && !submission.getCorrectedCode()[0].isEmpty()) {

            VBox correctedBox = new VBox();
            correctedBox.setSpacing(10);
            correctedBox.getStyleClass().add("code-section");
            Label correctedLabel = new Label("Corrected Solution:");
            correctedLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

// Use WebView to display HTML formatted corrected code
            WebView correctedWebView = new WebView();
            correctedWebView.setContextMenuEnabled(false);
            String correctedCode = submission.getCorrectedCode()[0];
            String finalHtml = CodeHighlighter.addSyntaxHighlighting(HtmlDisplayer.createStyledHtml(correctedCode));
            correctedWebView.getEngine().loadContent(finalHtml);
            correctedWebView.prefWidthProperty().bind(contentBox.widthProperty());
            correctedWebView.setPrefHeight(1); // Set a minimal height
            correctedWebView.getEngine().documentProperty().addListener((obs, oldDoc, newDoc) -> {
                if (newDoc != null) {
                    correctedWebView.getEngine().executeScript("document.body.setAttribute('contenteditable', false);");
                    Object result = correctedWebView.getEngine().executeScript("document.documentElement.scrollHeight");
                    if (result instanceof Number) {
                        double contentHeight = ((Number) result).doubleValue();
                        correctedWebView.setPrefHeight(contentHeight);
                    }
                }
            });

            correctedBox.getChildren().addAll(correctedLabel, correctedWebView);
            contentBox.getChildren().add(correctedBox);
        }

        // Add all sections to content box
        contentBox.getChildren().addAll(challengeDescBox, solutionBox, feedbackBox);

        // Assemble the card
        card.getChildren().addAll(header, contentBox);

        return card;
    }

    @FXML
    private void handleBack() {

    }

    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Results");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("HTML Files", "*.html"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = fileChooser.showSaveDialog(resultsContainer.getScene().getWindow());
        if (file != null) {
            try {
                exportResults(file);
                showInfoAlert("Export Successful", "Results exported successfully to " + file.getName());
            } catch (IOException e) {
                showErrorAlert("Export Error", "Failed to export results: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void exportResults(File file) throws IOException {
        boolean isHtml = file.getName().toLowerCase().endsWith(".html");
        StringBuilder content = new StringBuilder();

        if (isHtml) {
            content.append("<!DOCTYPE html>\n<html>\n<head>\n");
            content.append("<title>Competition Results</title>\n");
            content.append("<style>\n");
            content.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
            content.append(".result-card { border: 1px solid #ccc; border-radius: 5px; margin-bottom: 20px; }\n");
            content.append(".result-header { background-color: #4760f4; color: white; padding: 15px; border-radius: 5px 5px 0 0; display: flex; justify-content: space-between; }\n");
            content.append(".result-content { padding: 15px; }\n");
            content.append(".feedback-section { background-color: #f0f9ff; padding: 15px; border: 1px solid #bae6fd; border-radius: 5px; margin: 10px 0; }\n");
            content.append(".code-section { background-color: #f8fafc; padding: 15px; border: 1px solid #e2e8f0; border-radius: 5px; margin: 10px 0; }\n");
            content.append(".code-block { font-family: monospace; background-color: #282c34; color: #abb2bf; padding: 10px; border-radius: 5px; white-space: pre-wrap; }\n");
            content.append(".rating-high { background-color: #4CAF50; color: white; padding: 5px 10px; border-radius: 5px; font-weight: bold; }\n");
            content.append(".rating-medium { background-color: #FF9800; color: white; padding: 5px 10px; border-radius: 5px; font-weight: bold; }\n");
            content.append(".rating-low { background-color: #F44336; color: white; padding: 5px 10px; border-radius: 5px; font-weight: bold; }\n");
            content.append("</style>\n");
            content.append("</head>\n<body>\n");

            content.append("<h1>").append(competitionName).append(" - Results</h1>\n");

            if (!challengeSubmissionMap.isEmpty()) {
                Submission firstSubmission = challengeSubmissionMap.values().iterator().next();
                content.append("<p><strong>Competition Date:</strong> ").append(formatDate(firstSubmission.getDate())).append("</p>\n");
                content.append("<p><strong>Completion Time:</strong> ").append(formatMinutes(firstSubmission.getTimeTaken())).append("</p>\n");
            }

            content.append("<p><strong>Total Score:</strong> ").append(totalScore).append("/").append(maxPossibleScore).append("</p>\n");

            for (Map.Entry<Challenge, Submission> entry : challengeSubmissionMap.entrySet()) {
                Challenge challenge = entry.getKey();
                Submission submission = entry.getValue();

                content.append("<div class='result-card'>\n");

                // Header
                content.append("<div class='result-header'>\n");
                content.append("<div>").append(challenge.getTitle()).append("</div>\n");

                // Rating
                int rating = submission.getRating();
                String ratingClass = "rating-high";
                if (rating <= 7) {
                    ratingClass = "rating-low";
                } else if (rating <= 14) {
                    ratingClass = "rating-medium";
                }

                content.append("<div class='").append(ratingClass).append("'>").append(rating).append(" / 20</div>\n");
                content.append("</div>\n"); // End header

                // Content
                content.append("<div class='result-content'>\n");

                // Challenge description
                content.append("<h3>Challenge Description:</h3>\n");
                content.append("<p>").append(challenge.getContent()).append("</p>\n");

                // User solution
                content.append("<div class='code-section'>\n");
                content.append("<h3>Your Solution:</h3>\n");
                content.append("<pre class='code-block'>").append(submission.getStudentTry()).append("</pre>\n");
                content.append("</div>\n");

                // AI Feedback
                content.append("<div class='feedback-section'>\n");
                content.append("<h3>AI Feedback:</h3>\n");
                content.append("<div>").append(submission.getAiFeedback() != null && submission.getAiFeedback().length > 0 ?
                        submission.getAiFeedback()[0] : "No feedback available").append("</div>\n");
                content.append("</div>\n");

                // Corrected solution if available
                if (submission.getCorrectedCode() != null && submission.getCorrectedCode().length > 0 &&
                        submission.getCorrectedCode()[0] != null && !submission.getCorrectedCode()[0].isEmpty()) {
                    content.append("<div class='code-section'>\n");
                    content.append("<h3>Corrected Solution:</h3>\n");
                    content.append("<pre class='code-block'>").append(submission.getCorrectedCode()[0]).append("</pre>\n");
                    content.append("</div>\n");
                }

                content.append("</div>\n"); // End content
                content.append("</div>\n"); // End card
            }

            content.append("</body>\n</html>");
        } else {
            // Plain text export
            content.append("Competition Results: ").append(competitionName).append("\n\n");

            if (!challengeSubmissionMap.isEmpty()) {
                Submission firstSubmission = challengeSubmissionMap.values().iterator().next();
                content.append("Competition Date: ").append(formatDate(firstSubmission.getDate())).append("\n");
                content.append("Completion Time: ").append(formatMinutes(firstSubmission.getTimeTaken())).append("\n");
            }

            content.append("Total Score: ").append(totalScore).append("/").append(maxPossibleScore).append("\n\n");

            for (Map.Entry<Challenge, Submission> entry : challengeSubmissionMap.entrySet()) {
                Challenge challenge = entry.getKey();
                Submission submission = entry.getValue();

                content.append("===== ").append(challenge.getTitle()).append(" =====\n");
                content.append("Score: ").append(submission.getRating()).append(" / 20\n\n");

                content.append("Challenge Description:\n");
                content.append(challenge.getContent()).append("\n\n");

                content.append("Your Solution:\n");
                content.append(submission.getStudentTry()).append("\n\n");

                content.append("AI Feedback:\n");
                content.append(submission.getAiFeedback() != null && submission.getAiFeedback().length > 0 ?
                        submission.getAiFeedback()[0] : "No feedback available").append("\n\n");

                if (submission.getCorrectedCode() != null && submission.getCorrectedCode().length > 0 &&
                        submission.getCorrectedCode()[0] != null && !submission.getCorrectedCode()[0].isEmpty()) {
                    content.append("Corrected Solution:\n");
                    content.append(submission.getCorrectedCode()[0]).append("\n\n");
                }

                content.append("--------------------------------------------------\n\n");
            }
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content.toString());
        }
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
    }

    private String formatMinutes(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;

        if (hours > 0) {
            return hours + " hr " + mins + " min";
        } else {
            return mins + " min";
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void Logout(ActionEvent actionEvent) {
        Session.clear();
        Navigator.showAlert(Alert.AlertType.INFORMATION,"See you soon ","You are going to logout");
        Navigator.redirect(actionEvent,"/fxml/user/Login.fxml");
    }

    public void Profile(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/user/Profile.fxml");
    }
    public void Comp(ActionEvent actionEvent) {
        Navigator.redirect(actionEvent,"/fxml/competitions_list.fxml");
    }
}