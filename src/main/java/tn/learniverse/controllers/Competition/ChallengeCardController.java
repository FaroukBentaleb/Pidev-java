package tn.learniverse.controllers.Competition;

import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;

import  tn.learniverse.entities.Challenge;
import  tn.learniverse.tools.RichTextUtils;

public class ChallengeCardController {
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label challengeNumberLabel;
    
    @FXML
    private MFXScrollPane descriptionScrollPane;
    
    @FXML
    private MFXScrollPane contentScrollPane;
    
    @FXML
    private MFXScrollPane solutionScrollPane;
    
    private int challengeNumber = 1;
    
    public void setChallenge(Challenge challenge) {
        if (challenge == null) return;
        
        // Set title and challenge number
        titleLabel.setText(challenge.getTitle());
        challengeNumberLabel.setText(String.valueOf(challengeNumber));
        
        // Set description with custom styling
        WebView descriptionView = new WebView();
        descriptionView.getEngine().loadContent(addCustomStyling(challenge.getDescription()));
        descriptionScrollPane.setContent(descriptionView);
        
        // Set content with custom styling
        WebView contentView = new WebView();
        contentView.getEngine().loadContent(addCustomStyling(challenge.getContent()));
        contentScrollPane.setContent(contentView);
        
        // Set solution with custom styling
        WebView solutionView = new WebView();
        solutionView.getEngine().loadContent(addCustomStyling(challenge.getSolution()));
        solutionScrollPane.setContent(solutionView);
    }
    
    /**
     * Sets the challenge number to display in the badge
     */
    public void setChallengeNumber(int number) {
        this.challengeNumber = number;
        if (challengeNumberLabel != null) {
            challengeNumberLabel.setText(String.valueOf(number));
        }
    }
    
    /**
     * Adds custom styling to the HTML content for better display
     */
    private String addCustomStyling(String htmlContent) {
        // If the content is null or empty, return a placeholder
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "<html><body style='font-family: Segoe UI, Arial, sans-serif; color: #64748b; padding: 8px;'>" +
                   "<p><i>No content provided</i></p></body></html>";
        }
        
        // Add custom styling to the body
        String styledHtml = htmlContent;
        if (!styledHtml.contains("<style>")) {
            styledHtml = styledHtml.replace("<body", 
                "<style>" +
                "body { font-family: 'Segoe UI', Arial, sans-serif; color: #334155; padding: 8px; font-size: 14px; }" +
                "p { line-height: 1.5; margin-bottom: 12px; }" +
                "h1, h2, h3, h4, h5, h6 { color: #1e293b; margin-top: 16px; margin-bottom: 12px; }" +
                "code { background-color: #f1f5f9; padding: 2px 4px; border-radius: 4px; font-family: monospace; }" +
                "pre { background-color: #f1f5f9; padding: 12px; border-radius: 8px; overflow: auto; }" +
                "ul, ol { padding-left: 20px; margin-bottom: 12px; }" +
                "li { margin-bottom: 4px; }" +
                "a { color: #4f46e5; text-decoration: none; }" +
                "a:hover { text-decoration: underline; }" +
                "img { max-width: 100%; border-radius: 4px; }" +
                "</style><body");
        }
        
        return styledHtml;
    }
} 