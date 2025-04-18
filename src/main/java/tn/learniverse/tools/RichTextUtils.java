package tn.learniverse.tools;
import javafx.scene.web.WebView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * Utility class for rich text handling operations
 */
public class RichTextUtils {
    
    /**
     * Extracts plain text from HTML content
     * 
     * @param html The HTML content to extract text from
     * @return The plain text content with HTML tags removed
     */
    public static String extractTextFromHtml(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        
        // Basic HTML stripping - for more accuracy, consider using a proper HTML parser
        String text = html;
        
        // Remove HTML tags
        text = text.replaceAll("<[^>]*>", "");
        
        // Convert HTML entities
        text = text.replace("&nbsp;", " ")
                   .replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&amp;", "&")
                   .replace("&quot;", "\"")
                   .replace("&apos;", "'");
        
        // Trim and normalize whitespace
        text = text.trim().replaceAll("\\s+", " ");
        
        return text;
    }
    
    /**
     * Creates a WebView component to display HTML content
     * 
     * @param htmlContent The HTML content to display
     * @param height The height of the WebView component
     * @return A ScrollPane containing the WebView
     */
    public static ScrollPane createHtmlViewer(String htmlContent, double height) {
        WebView webView = new WebView();
        webView.getEngine().loadContent(htmlContent);
        webView.setPrefHeight(height);
        
        // Disable context menu and web navigation
        webView.setContextMenuEnabled(false);
        
        // Create a scrollable container
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(webView);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(height + 5); // Add a bit extra for scrollbar
        
        return scrollPane;
    }
    
    /**
     * Converts plain text to HTML for display
     * 
     * @param text The plain text to convert
     * @return HTML formatted content
     */
    public static String textToHtml(String text) {
        if (text == null || text.isEmpty()) {
            return "<html><body></body></html>";
        }
        
        // Escape HTML special characters
        String htmlText = text.replace("&", "&amp;")
                              .replace("<", "&lt;")
                              .replace(">", "&gt;")
                              .replace("\"", "&quot;")
                              .replace("'", "&apos;")
                              .replace("\n", "<br>");
        
        return "<html><body style='font-family: Arial; font-size: 12px;'><p>" + 
                htmlText + "</p></body></html>";
    }
    
    /**
     * Creates an empty HTML document with default styling
     * 
     * @param placeholder Placeholder text to show
     * @return HTML formatted content with placeholder
     */
    public static String createEmptyHtml(String placeholder) {
        return "<html><body style='font-family: Arial; font-size: 12px;'><p>" + 
                placeholder + "</p></body></html>";
    }
    
    /**
     * Creates styled HTML content from plain text
     * @param text The plain text to format
     * @return HTML formatted string with proper styling
     */
    public static String createStyledHtml(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "<div style='color: #666; font-style: italic;'>No content available</div>";
        }
        
        // Basic HTML template with styling - Escape % chars with %%
        String htmlTemplate = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: 'Roboto', Arial, sans-serif;
                        font-size: 14px;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                    }
                    pre {
                        background-color: #f5f5f5;
                        border-radius: 4px;
                        padding: 12px;
                        overflow-x: auto;
                        font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
                        font-size: 13px;
                        line-height: 1.4;
                        margin: 8px 0;
                    }
                    code {
                        background-color: #f5f5f5;
                        padding: 2px 4px;
                        border-radius: 3px;
                        font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
                        font-size: 13px;
                    }
                    p {
                        margin: 8px 0;
                    }
                    ul, ol {
                        margin: 8px 0;
                        padding-left: 24px;
                    }
                    h1, h2, h3, h4, h5, h6 {
                        margin: 16px 0 8px 0;
                        color: #2196f3;
                    }
                    blockquote {
                        border-left: 4px solid #2196f3;
                        margin: 8px 0;
                        padding: 8px 16px;
                        background-color: #f5f5f5;
                    }
                    a {
                        color: #2196f3;
                        text-decoration: none;
                    }
                    a:hover {
                        text-decoration: underline;
                    }
                    table {
                        border-collapse: collapse;
                        width: 100%%;
                        margin: 8px 0;
                    }
                    th, td {
                        border: 1px solid #ddd;
                        padding: 8px;
                        text-align: left;
                    }
                    th {
                        background-color: #f5f5f5;
                    }
                </style>
            </head>
            <body>
                %s
            </body>
            </html>
            """;
        
        // Escape HTML special characters
        String escapedText = text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
        
        // Convert line breaks to <br> tags
        String formattedText = escapedText.replace("\n", "<br>");
        
        return String.format(htmlTemplate, formattedText);
    }
} 