package com.esprit.java.controllers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import com.esprit.java.Utility.CodeHighlighter;

/**
 * An extension of HTMLEditor with additional features like code block insertion
 * and simplified toolbar options
 */
public class CustomHtmlEditor extends HTMLEditor {
    
    private Button codeBlockButton;
    private WebView webView;
    private boolean simplified = false;
    
    // Button IDs to keep in simplified mode
    private static final List<String> SIMPLIFIED_BUTTONS = Arrays.asList(
        "bold", "italic", "underline", 
        "paragraphs", "lists",
        "separator"
    );
    
    // Button IDs to keep in code editor mode (for solution field)
    private static final List<String> CODE_EDITOR_BUTTONS = Arrays.asList(
        "separator"
    );
    
    public CustomHtmlEditor() {
        this(false, false);
    }
    
    public CustomHtmlEditor(boolean simplified, boolean codeEditorMode) {
        super();
        this.simplified = simplified;
        
        // Apply initial styling
        this.setStyle("-fx-border-radius: 6px; -fx-background-radius: 6px; -fx-border-color: #D1DBE0;");
        
        // Call after constructor to ensure HTMLEditor is fully initialized
        Platform.runLater(() -> {
            // Add code block button first
            addCodeBlockButton();
            
            // Then simplify the toolbar if needed
            if (simplified) {
                if (codeEditorMode) {
                    simplifyToolbar(CODE_EDITOR_BUTTONS);
                } else {
                    simplifyToolbar(SIMPLIFIED_BUTTONS);
                }
            }
            
            // Get access to internal WebView for direct manipulation
            try {
                webView = (WebView) lookup("WebView");
                
                // Add key event handler for tab key (otherwise it changes focus)
                webView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.TAB) {
                        handleTabKey(event);
                    }
                });
                
                // Set a fixed height for the WebView to prevent it from growing too large
                webView.setPrefHeight(150);
                
                // Add focus styles without animations
                setupFocusHandling();
                
            } catch (Exception e) {
                System.err.println("Could not access WebView: " + e.getMessage());
            }
            
            // Enhance toolbar styling
            enhanceToolbarStyling();
        });
    }
    
    /**
     * Setup focus handling without animations
     */
    private void setupFocusHandling() {
        // Listen for focus changes
        this.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Focused: change border color
                this.setStyle("-fx-border-color: #0056D2; -fx-border-radius: 6px; -fx-background-radius: 6px;");
            } else {
                // Not focused: revert styling
                this.setStyle("-fx-border-color: #D1DBE0; -fx-border-radius: 6px; -fx-background-radius: 6px;");
            }
        });
    }
    
    /**
     * Enhance toolbar visual styling
     */
    private void enhanceToolbarStyling() {
        try {
            // Find the top toolbar
            ToolBar topToolbar = (ToolBar) lookup(".top-toolbar");
            if (topToolbar != null) {
                topToolbar.setStyle("-fx-background-color: #F5F7F8; -fx-border-color: #D1DBE0; -fx-border-width: 0 0 1 0; " +
                                   "-fx-border-radius: 6px 6px 0 0; -fx-background-radius: 6px 6px 0 0;");
                
                // Enhance button styling
                for (Node node : topToolbar.getItems()) {
                    if (node instanceof ButtonBase) {
                        ButtonBase button = (ButtonBase) node;
                        
                        // Add hover and pressed effects
                        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #E8EBED;"));
                        button.setOnMouseExited(e -> button.setStyle(""));
                        button.setOnMousePressed(e -> button.setStyle("-fx-background-color: #D1DBE0;"));
                        button.setOnMouseReleased(e -> button.setStyle("-fx-background-color: #E8EBED;"));
                    } else if (node instanceof Separator) {
                        node.setStyle("-fx-background-color: #D1DBE0;");
                    }
                }
                
                // Make sure code block button has the same styling
                if (codeBlockButton != null) {
                    codeBlockButton.setStyle("-fx-cursor: hand;");
                }
            }
        } catch (Exception e) {
            System.err.println("Could not enhance toolbar styling: " + e.getMessage());
        }
    }
    
    /**
     * Handles tab key inside code blocks to properly indent code
     */
    private void handleTabKey(KeyEvent event) {
        // Get the current selection position
        String script = "document.designMode === 'on' && document.activeElement.isContentEditable";
        
        try {
            Boolean isEditable = (Boolean) webView.getEngine().executeScript(script);
            
            if (isEditable) {
                // Insert tab character (or spaces)
                webView.getEngine().executeScript(
                    "document.execCommand('insertHTML', false, '    ');"
                );
                event.consume(); // Prevent default tab behavior
            }
        } catch (Exception e) {
            // If script execution fails, let the default behavior happen
        }
    }
    
    /**
     * Simplifies the toolbar by keeping only the specified buttons
     */
    private void simplifyToolbar(List<String> buttonsToKeep) {
        try {
            // Find the top toolbar
            ToolBar topToolbar = (ToolBar) lookup(".top-toolbar");
            if (topToolbar != null) {
                // Create a copy of the items to avoid concurrent modification
                List<Node> originalItems = topToolbar.getItems();
                for (int i = originalItems.size() - 1; i >= 0; i--) {
                    Node item = originalItems.get(i);
                    
                    // Keep only certain buttons
                    boolean keep = false;
                    
                    // Keep separators if specified
                    if (item instanceof Separator && buttonsToKeep.contains("separator")) {
                        keep = true;
                    }
                    // Keep code block button we added (it has a tooltip with specific text)
                    else if (item instanceof Button && ((Button) item).getTooltip() != null && 
                             ((Button) item).getTooltip().getText().equals("Insert Code Block")) {
                        keep = true;
                    }
                    // Check built-in buttons using their style class or other properties
                    else if (item instanceof ButtonBase) {
                        for (String buttonId : buttonsToKeep) {
                            if (item.getStyleClass().contains(buttonId) || 
                                    (item.getId() != null && item.getId().contains(buttonId))) {
                                keep = true;
                                break;
                            }
                        }
                    }
                    
                    if (!keep) {
                        // Remove the item if not in our "keep" list
                        topToolbar.getItems().remove(i);
                    }
                }
            }
            
            // Find and remove bottom toolbar completely for simplified mode
            ToolBar bottomToolbar = (ToolBar) lookup(".bottom-toolbar");
            if (bottomToolbar != null) {
                bottomToolbar.setVisible(false);
                bottomToolbar.setManaged(false);
            }
            
        } catch (Exception e) {
            System.err.println("Could not simplify toolbar: " + e.getMessage());
        }
    }
    
    /**
     * Adds a code block button to the toolbar
     */
    private void addCodeBlockButton() {
        try {
            // Find the top toolbar
            ToolBar toolbar = (ToolBar) lookup(".top-toolbar");
            
            if (toolbar != null) {
                // Create code block button
                codeBlockButton = new Button();
                codeBlockButton.setTooltip(new Tooltip("Insert Code Block"));
                
                // Use code icon (<> symbol)
                try {
                    Image codeIcon = new Image(getClass().getResourceAsStream("/images/code-icon.png"));
                    codeBlockButton.setGraphic(new ImageView(codeIcon));
                } catch (Exception e) {
                    // Fallback to text if icon not found
                    codeBlockButton.setText("<>");
                }
                
                // Style the button
                codeBlockButton.setStyle("-fx-cursor: hand;");
                
                // Add hover effects
                codeBlockButton.setOnMouseEntered(e -> {
                    codeBlockButton.setStyle("-fx-background-color: #E8EBED; -fx-cursor: hand;");
                });
                codeBlockButton.setOnMouseExited(e -> {
                    codeBlockButton.setStyle("-fx-cursor: hand;");
                });
                
                // Add action
                codeBlockButton.setOnAction(this::handleCodeBlockAction);
                
                // Add separator and button to toolbar
                Separator separator = new Separator();
                separator.setStyle("-fx-background-color: #D1DBE0;");
                toolbar.getItems().add(separator);
                toolbar.getItems().add(codeBlockButton);
            }
        } catch (Exception e) {
            System.err.println("Could not add code block button: " + e.getMessage());
        }
    }
    
    /**
     * Handles the code block button action
     */
    private void handleCodeBlockAction(ActionEvent event) {
        try {
            WebView webView = (WebView) lookup("WebView");
            if (webView != null) {
                String codeHtml = CodeHighlighter.showCodeDialog();
                
                if (codeHtml != null && !codeHtml.isEmpty()) {
                    // Direct JavaScript injection approach
                    String script = """
                        (function() {
                            var sel = window.getSelection();
                            if (sel.rangeCount > 0) {
                                var range = sel.getRangeAt(0);
                                var div = document.createElement('div');
                                div.innerHTML = `%s`;
                                
                                // Clear any existing selection
                                range.deleteContents();
                                
                                // Insert the code block
                                var fragment = document.createDocumentFragment();
                                while (div.firstChild) {
                                    fragment.appendChild(div.firstChild);
                                }
                                range.insertNode(fragment);
                                
                                // Move cursor to end
                                range.collapse(false);
                                sel.removeAllRanges();
                                sel.addRange(range);
                                
                                // Force update
                                document.execCommand('insertText', false, '');
                            }
                        })();
                    """.formatted(codeHtml.replace("`", "\\`"));
                    
                    webView.getEngine().executeScript(script);
                    
                    // Log success
                    System.out.println("Code block inserted successfully");
                }
            } else {
                System.err.println("WebView not found");
            }
        } catch (Exception e) {
            System.err.println("Error inserting code block: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Executes an HTML command on the editor
     */
    private void executeHTMLCommand(String command, String value) {
        try {
            WebView webView = (WebView) lookup("WebView");
            if (webView != null) {
                if ("insertHTML".equals(command) && value != null) {
                    // Escape special characters
                    String escapedValue = value.replace("`", "\\`")
                                             .replace("$", "\\$");
                    
                    String script = """
                        (function() {
                            try {
                                var sel = window.getSelection();
                                if (sel.rangeCount > 0) {
                                    var range = sel.getRangeAt(0);
                                    var div = document.createElement('div');
                                    div.innerHTML = `%s`;
                                    
                                    range.deleteContents();
                                    var fragment = document.createDocumentFragment();
                                    while (div.firstChild) {
                                        fragment.appendChild(div.firstChild);
                                    }
                                    range.insertNode(fragment);
                                    
                                    // Move cursor to end
                                    range.collapse(false);
                                    sel.removeAllRanges();
                                    sel.addRange(range);
                                    
                                    return true;
                                }
                                return false;
                            } catch (e) {
                                console.error('Error:', e);
                                return false;
                            }
                        })();
                    """.formatted(escapedValue);
                    
                    Object result = webView.getEngine().executeScript(script);
                    System.out.println("Insert HTML result: " + result);
                } else {
                    // For other commands, use standard execCommand
                    webView.getEngine().executeScript(
                        String.format("document.execCommand('%s', false, %s);",
                            command,
                            value != null ? "'" + value.replace("'", "\\'") + "'" : "null"
                        )
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing HTML command: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get properly formatted HTML content with code highlighting
     */
    @Override
    public String getHtmlText() {
        String html = super.getHtmlText();
        
        // Clean up HTML if needed
        html = html.replace("<meta charset=\"utf-8\">", "<meta charset=\"UTF-8\">");
        
        // Fix any potential issues with code blocks
        try {
            WebView webView = (WebView) lookup("WebView");
            if (webView != null) {
                // This script fixes potential formatting issues with code blocks
                String fixScript = """
                    (function() {
                        // Find all code blocks and ensure they have proper styling
                        var codeBlocks = document.querySelectorAll('code[class^="language-"]');
                        for (var i = 0; i < codeBlocks.length; i++) {
                            var block = codeBlocks[i];
                            var pre = block.closest('pre');
                            
                            // Ensure pre has proper styling
                            if (pre && !pre.getAttribute('style')) {
                                pre.style.backgroundColor = '#f5f5f5';
                                pre.style.padding = '10px';
                                pre.style.borderRadius = '5px';
                                pre.style.margin = '10px 0';
                                pre.style.fontFamily = 'monospace';
                                pre.style.overflowX = 'auto';
                            }
                            
                            // Ensure code has proper styling
                            if (!block.getAttribute('style')) {
                                block.style.display = 'block';
                                block.style.whiteSpace = 'pre';
                                block.style.fontFamily = 'Courier New, monospace';
                            }
                        }
                        return document.documentElement.outerHTML;
                    })();
                """;
                
                // Try to execute the fix script
                try {
                    Object result = webView.getEngine().executeScript(fixScript);
                    if (result != null && result instanceof String) {
                        // Extract the body content only
                        String fullHtml = (String) result;
                        if (fullHtml.contains("<body>") && fullHtml.contains("</body>")) {
                            int start = fullHtml.indexOf("<body>") + 6;
                            int end = fullHtml.indexOf("</body>");
                            if (start > 0 && end > start) {
                                String bodyContent = fullHtml.substring(start, end).trim();
                                return bodyContent;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Silently continue with the original HTML if script fails
                    System.err.println("Failed to fix code blocks: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // Continue with the original HTML
            System.err.println("Error accessing WebView: " + e.getMessage());
        }
        
        return html;
    }
    
    /**
     * Get the JavaFX property for the HTML text
     * This allows binding to the HTML content
     */
    public javafx.beans.property.StringProperty htmlTextProperty() {
        // Use reflection to get the htmlText property from the parent class
        try {
            java.lang.reflect.Field field = HTMLEditor.class.getDeclaredField("htmlText");
            field.setAccessible(true);
            return (javafx.beans.property.StringProperty) field.get(this);
        } catch (Exception e) {
            // If reflection fails, create a new property (note: this won't be linked to parent)
            System.err.println("Could not access htmlText property: " + e.getMessage());
            return new javafx.beans.property.SimpleStringProperty(this.getHtmlText());
        }
    }
    
    /**
     * Sets the preferred height of the editor
     */
    public void setCustomPrefHeight(double height) {
        this.setPrefHeight(height);
        
        // Adjust inner WebView height as well
        if (webView != null) {
            // Calculate WebView height (editor height minus toolbar height)
            ToolBar toolbar = (ToolBar) lookup(".top-toolbar");
            double toolbarHeight = toolbar != null ? toolbar.getHeight() : 36;
            webView.setPrefHeight(height - toolbarHeight);
        }
    }
} 