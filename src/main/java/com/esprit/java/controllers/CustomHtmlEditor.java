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
        String codeHtml = CodeHighlighter.showCodeDialog();
        
        if (codeHtml != null && !codeHtml.isEmpty()) {
            // Insert the HTML for the code block at the current cursor position
            executeHTMLCommand("insertHTML", codeHtml);
        }
    }
    
    /**
     * Executes an HTML command on the editor
     */
    private void executeHTMLCommand(String command, String value) {
        try {
            // Use reflection to access protected executeHTMLCommand method
            Method method = HTMLEditor.class.getDeclaredMethod("executeHTMLCommand", String.class, String.class);
            method.setAccessible(true);
            method.invoke(this, command, value);
        } catch (Exception e) {
            // Fallback to direct JavaScript execution if reflection fails
            WebView webView = (WebView) lookup("WebView");
            if (webView != null) {
                webView.getEngine().executeScript(
                    "document.execCommand('" + command + "', false, " + 
                    (value != null ? "'" + value.replace("'", "\\'") + "'" : "null") + ");"
                );
            }
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
        
        return html;
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