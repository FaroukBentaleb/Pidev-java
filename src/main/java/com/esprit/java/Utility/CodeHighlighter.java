package com.esprit.java.Utility;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Utility class for code highlighting in JavaFX applications
 */
public class CodeHighlighter {
    
    // Available language options for syntax highlighting
    private static final String[] LANGUAGES = {
        "markup", "html", "xml", "svg", "mathml", 
        "css", "clike", "javascript", "js",
        "java", "php", "python", "ruby", "go", "csharp", "c", "cpp",
        "sql", "bash", "powershell", "json", "yaml", "markdown", 
        "typescript", "kotlin", "swift", "dart", "plsql"
    };
    
    /**
     * Shows a dialog for inserting formatted code
     * 
     * @return HTML formatted code block with syntax highlighting markup
     */
    public static String showCodeDialog() {
        // Create a new dialog stage
        Stage dialog = new Stage();
        dialog.setTitle("Insert Code");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setMinWidth(600);
        dialog.setMinHeight(400);
        
        // Language selector
        ComboBox<String> languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll(LANGUAGES);
        languageSelector.setValue("java"); // Default to Java
        languageSelector.setPromptText("Select Language");
        
        // Code input area
        TextArea codeInput = new TextArea();
        codeInput.setPromptText("Paste your code here...");
        codeInput.setWrapText(true);
        codeInput.setPrefHeight(300);
        VBox.setVgrow(codeInput, Priority.ALWAYS);
        
        // Preview button and area
        Button previewButton = new Button("Preview");
        WebView previewArea = new WebView();
        previewArea.setPrefHeight(200);
        VBox.setVgrow(previewArea, Priority.SOMETIMES);
        
        // Preview functionality
        previewButton.setOnAction(e -> {
            String code = codeInput.getText();
            String language = languageSelector.getValue();
            previewArea.getEngine().loadContent(getPreviewHtml(code, language));
        });
        
        // Buttons
        Button insertButton = new Button("Insert");
        Button cancelButton = new Button("Cancel");
        insertButton.setDefaultButton(true);
        cancelButton.setCancelButton(true);
        
        // Layout
        HBox languageBox = new HBox(10, new Label("Language:"), languageSelector, previewButton);
        languageBox.setPadding(new Insets(10));
        
        HBox buttonBox = new HBox(10, insertButton, cancelButton);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setStyle("-fx-alignment: center-right;");
        
        VBox codeBox = new VBox(10, new Label("Code:"), codeInput);
        codeBox.setPadding(new Insets(0, 10, 10, 10));
        
        VBox previewBox = new VBox(10, new Label("Preview:"), previewArea);
        previewBox.setPadding(new Insets(0, 10, 10, 10));
        
        VBox mainLayout = new VBox(10);
        mainLayout.getChildren().addAll(languageBox, codeBox, previewBox, buttonBox);
        
        // Set the scene
        Scene scene = new Scene(mainLayout);
        dialog.setScene(scene);
        
        // Result container
        final String[] result = {null};
        
        // Button actions
        insertButton.setOnAction(e -> {
            String code = codeInput.getText();
            String language = languageSelector.getValue();
            result[0] = formatCodeForHtml(code, language);
            dialog.close();
        });
        
        cancelButton.setOnAction(e -> dialog.close());
        
        // Show dialog and wait for it to be closed
        dialog.showAndWait();
        
        return result[0];
    }
    
    /**
     * Formats code with HTML and CSS for syntax highlighting
     * 
     * @param code The source code
     * @param language The programming language
     * @return HTML formatted code ready for insertion
     */
    private static String formatCodeForHtml(String code, String language) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        // Escape HTML special characters
        String escapedCode = code.replace("&", "&amp;")
                                .replace("<", "&lt;")
                                .replace(">", "&gt;")
                                .replace("\"", "&quot;")
                                .replace("'", "&apos;");
        
        // Create code block with language class for syntax highlighting
        return "<pre><code class=\"language-" + language + "\">" + escapedCode + "</code></pre>";
    }
    
    /**
     * Creates HTML for previewing code with syntax highlighting
     * 
     * @param code The source code
     * @param language The programming language
     * @return Complete HTML document with Prism.js for preview
     */
    private static String getPreviewHtml(String code, String language) {
        String escapedCode = code.replace("&", "&amp;")
                                .replace("<", "&lt;")
                                .replace(">", "&gt;")
                                .replace("\"", "&quot;")
                                .replace("'", "&apos;");
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism.min.css">
                <style>
                    body { margin: 8px; font-family: sans-serif; }
                    pre { margin: 0; }
                </style>
            </head>
            <body>
                <pre><code class="language-%s">%s</code></pre>
                
                <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-core.min.js"></script>
                <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/plugins/autoloader/prism-autoloader.min.js"></script>
            </body>
            </html>
            """.formatted(language, escapedCode);
    }
    
    /**
     * Injects Prism.js library into HTML content for syntax highlighting
     * 
     * @param htmlContent The HTML content that may contain code blocks
     * @return HTML with Prism.js included for syntax highlighting
     */
    public static String addSyntaxHighlighting(String htmlContent) {
        // If the content doesn't have code blocks, return as is
        if (!htmlContent.contains("<code class=\"language-")) {
            return htmlContent;
        }
        
        // If content already has the full HTML structure
        if (htmlContent.toLowerCase().contains("<!doctype html>") || 
            htmlContent.toLowerCase().contains("<html")) {
            
            // Insert Prism.js before the closing head tag
            String prismCss = "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism.min.css\">";
            String prismJs = """
                <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-core.min.js"></script>
                <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/plugins/autoloader/prism-autoloader.min.js"></script>
                """;
            
            // Add CSS to head
            htmlContent = htmlContent.replaceFirst("(?i)</head>", prismCss + "</head>");
            
            // Add JS before body closing
            htmlContent = htmlContent.replaceFirst("(?i)</body>", prismJs + "</body>");
            
            return htmlContent;
        } else {
            // Wrap content in a complete HTML document with Prism
            return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism.min.css">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; margin: 20px; color: #333; }
                        pre { margin-bottom: 20px; border-radius: 5px; background-color: #f5f5f5; }
                        code { font-family: 'Courier New', monospace; }
                    </style>
                </head>
                <body>
                    %s
                    
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-core.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/plugins/autoloader/prism-autoloader.min.js"></script>
                </body>
                </html>
                """.formatted(htmlContent);
        }
    }
} 