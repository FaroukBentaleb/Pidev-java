package tn.learniverse.controllers.Reclamation;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.learniverse.services.ChatBotReclamationService;

public class ChatBotReclamationApp extends Application {

    private TextArea chatArea;
    private TextField inputField;
    private final ChatBotReclamationService chatGPT = new ChatBotReclamationService();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chatbot Réclamation");

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        inputField = new TextField();
        inputField.setPromptText("Posez votre question ici...");

        Button sendButton = new Button("Envoyer");
        sendButton.setOnAction(e -> envoyerMessage());

        HBox inputBox = new HBox(10, inputField, sendButton);
        inputBox.setPadding(new Insets(10));

        VBox root = new VBox(10, chatArea, inputBox);
        root.setPadding(new Insets(10));

        chatArea.appendText("Chatbot : Bonjour ! Comment puis-je vous aider aujourd’hui avec votre problème ?\n");

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void envoyerMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            chatArea.appendText("Vous : " + message + "\n");
            inputField.clear();

            new Thread(() -> {
                String response = chatGPT.envoyer(message);
                javafx.application.Platform.runLater(() -> {
                    chatArea.appendText("Chatbot : " + response + "\n");
                });
            }).start();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

