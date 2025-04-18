package tn.learniverse.tools;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import tn.learniverse.tools.CodeHighlighter;

/**
 * A utility class for displaying HTML content in a separate window
 */
public class HtmlDisplayer {
    
    /**
     * Opens a new window displaying the HTML content
     * 
     * @param title The window title
     * @param htmlContent The HTML content to display
     * @param width The window width
     * @param height The window height
     */
    public static void showHtmlContent(String title, String htmlContent, int width, int height) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        
        // Apply syntax highlighting if needed
        String finalHtml = CodeHighlighter.addSyntaxHighlighting(htmlContent);
        
        // Create WebView to display HTML
        WebView webView = new WebView();
        webView.getEngine().loadContent(finalHtml);
        
        // Add a close button
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> stage.close());
        
        // Create a spacer for the button bar
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Create a button bar
        HBox buttonBar = new HBox(10, spacer, closeButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(10));
        
        // Create the layout
        BorderPane layout = new BorderPane();
        layout.setCenter(webView);
        layout.setBottom(buttonBar);
        
        // Set the scene
        Scene scene = new Scene(layout, width, height);
        stage.setScene(scene);
        
        // Show the window
        stage.show();
    }
    
    /**
     * Opens a new window displaying the HTML content with default dimensions
     * 
     * @param title The window title
     * @param htmlContent The HTML content to display
     */
    public static void showHtmlContent(String title, String htmlContent) {
        showHtmlContent(title, htmlContent, 800, 600);
    }
    
    /**
     * Creates styled HTML content from raw HTML
     * 
     * @param rawHtml The raw HTML content
     * @return The styled HTML content with CSS
     */
    public static String createStyledHtml(String rawHtml) {
        // If already has HTML structure, return as is
        if (rawHtml.toLowerCase().contains("<html")) {
            return CodeHighlighter.addSyntaxHighlighting(rawHtml);
        }
        
        // CSS styles for the HTML content
        String css = """
            <style>
                body {
                    font-family: Arial, sans-serif;
                    line-height: 1.6;
                    margin: 20px;
                    color: #333;
                }
                h1, h2, h3, h4, h5, h6 {
                    color: #2c3e50;
                    margin-top: 20px;
                }
                pre {
                    background-color: #f5f5f5;
                    padding: 10px;
                    border-radius: 5px;
                    overflow-x: auto;
                }
                code {
                    font-family: 'Courier New', monospace;
                    background-color: #f5f5f5;
                    padding: 2px 4px;
                    border-radius: 3px;
                }
                table {
                    border-collapse: collapse;
                    width: 100%;
                    margin: 15px 0;
                }
                th, td {
                    border: 1px solid #ddd;
                    padding: 8px;
                    text-align: left;
                }
                th {
                    background-color: #f2f2f2;
                }
                img {
                    max-width: 100%;
                    height: auto;
                }
            </style>
        """;
        
        // Add Prism.js CSS for code highlighting
        String prismCss = """
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism.min.css">
        """;
        
        // Add Prism.js scripts for syntax highlighting
        String prismJs = """
            <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-core.min.js"></script>
            <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/plugins/autoloader/prism-autoloader.min.js"></script>
        """;
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                %s
                %s
            </head>
            <body>
                %s
                %s
            </body>
            </html>
            """.formatted(css, prismCss, rawHtml, prismJs);
    }
} 