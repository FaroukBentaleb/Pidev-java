package com.esprit.java.Utility;

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
} 