package tn.learniverse.tools;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import  tn.learniverse.entities.Challenge;
import  tn.learniverse.entities.Competition;

/**
 * Helper utility for displaying competition and challenge content with proper formatting
 */
public class CompetitionViewHelper {
    
    /**
     * Opens a window to display the competition details with syntax highlighting
     * 
     * @param competition The competition to display
     */
    public static void showCompetition(Competition competition) {
        if (competition == null) {
            return;
        }
        
        // Create the main stage
        Stage stage = new Stage();
        stage.setTitle("Competition: " + competition.getNom());
        
        // Create a TabPane for competition details and challenges
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Add competition details tab
        Tab detailsTab = new Tab("Details");
        VBox detailsContent = createCompetitionDetailsView(competition);
        detailsTab.setContent(detailsContent);
        tabPane.getTabs().add(detailsTab);
        
        // Add challenges tabs if any
        if (competition.getChallenges() != null && !competition.getChallenges().isEmpty()) {
            for (Challenge challenge : competition.getChallenges()) {
                Tab challengeTab = new Tab("Challenge: " + challenge.getTitle());
                VBox challengeContent = createChallengeView(challenge);
                challengeTab.setContent(challengeContent);
                tabPane.getTabs().add(challengeTab);
            }
        }
        
        // Create the scene
        Scene scene = new Scene(tabPane, 900, 650);
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * Creates a view for displaying competition details
     * 
     * @param competition The competition to display
     * @return A VBox containing the formatted details
     */
    private static VBox createCompetitionDetailsView(Competition competition) {
        VBox content = new VBox(20);
        content.setStyle("-fx-padding: 20px;");
        
        // Create the HTML content with competition details
        String htmlContent = """
            <h1>%s</h1>
            <div class="meta-info">
                <p><strong>Category:</strong> %s</p>
                <p><strong>Status:</strong> %s</p>
                <p><strong>Starts:</strong> %s</p>
                <p><strong>Duration:</strong> %s minutes</p>
            </div>
            <h2>Description</h2>
            <div class="description">
                %s
            </div>
            """.formatted(
                competition.getNom(),
                competition.getCategorie(),
                competition.getEtat(),
                competition.getDateComp() != null ? competition.getDateComp().toString() : "Not set",
                competition.getDuration(),
                competition.getDescription()
            );
        
        // Apply syntax highlighting if needed
        String finalHtml = CodeHighlighter.addSyntaxHighlighting(
            HtmlDisplayer.createStyledHtml(htmlContent)
        );
        
        // Create a WebView to display the content
        WebView webView = new WebView();
        webView.getEngine().loadContent(finalHtml);
        
        ScrollPane scrollPane = new ScrollPane(webView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        content.getChildren().add(scrollPane);
        
        return content;
    }
    
    /**
     * Creates a view for displaying challenge details
     * 
     * @param challenge The challenge to display
     * @return A VBox containing the formatted details
     */
    private static VBox createChallengeView(Challenge challenge) {
        VBox content = new VBox(20);
        content.setStyle("-fx-padding: 20px;");
        
        // Create the HTML content with challenge details
        String htmlContent = """
            <h1>%s</h1>
            <h2>Description</h2>
            <div class="description">
                %s
            </div>
            <h2>Challenge</h2>
            <div class="content">
                %s
            </div>
            <h2>Expected Solution</h2>
            <pre><code class="language-java">%s</code></pre>
            """.formatted(
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getContent(),
                challenge.getSolution()
            );
        
        // Apply syntax highlighting
        String finalHtml = CodeHighlighter.addSyntaxHighlighting(
            HtmlDisplayer.createStyledHtml(htmlContent)
        );
        
        // Create a WebView to display the content
        WebView webView = new WebView();
        webView.getEngine().loadContent(finalHtml);
        
        ScrollPane scrollPane = new ScrollPane(webView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        content.getChildren().add(scrollPane);
        
        return content;
    }
    
    /**
     * Preview a single challenge in a separate window
     * 
     * @param challenge The challenge to preview
     */
    public static void previewChallenge(Challenge challenge) {
        if (challenge == null) {
            return;
        }
        
        VBox challengeView = createChallengeView(challenge);
        
        Stage stage = new Stage();
        stage.setTitle("Preview: " + challenge.getTitle());
        
        BorderPane layout = new BorderPane();
        layout.setCenter(challengeView);
        
        Scene scene = new Scene(layout, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
} 