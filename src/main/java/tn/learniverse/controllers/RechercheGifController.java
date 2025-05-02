package tn.learniverse.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class RechercheGifController {

    @FXML
    private TextField searchField;

    @FXML
    private FlowPane gifFlowPane;

    private String selectedGifUrl;
    private GifSelectionListener listener;

    public void initialize() {
        searchField.setOnAction(e -> rechercherGifs());
    }

    public void setGifSelectionListener(GifSelectionListener listener) {
        this.listener = listener;
    }

    private void rechercherGifs() {
        String query = searchField.getText();
        if (query.isEmpty()) return;

        new Thread(() -> {
            try {
                List<String> urls = searchGifs(query);
                Platform.runLater(() -> afficherGifs(urls));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private List<String> searchGifs(String query) throws IOException, InterruptedException {
        String apiKey = "uitgaAcN3LNhXruHEB2TIGuNdNvtBQhw"; // à remplacer par ta vraie clé
        String url = "https://api.giphy.com/v1/gifs/search?api_key=" + apiKey + "&q=" + query + "&limit=10";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        List<String> gifUrls = new ArrayList<>();
        for (JsonNode gif : root.get("data")) {
            String gifUrl = gif.get("images").get("downsized").get("url").asText();
            gifUrls.add(gifUrl);
        }
        return gifUrls;
    }

    private void afficherGifs(List<String> urls) {
        gifFlowPane.getChildren().clear();
        for (String url : urls) {
            ImageView imageView = new ImageView(new Image(url, 100, 100, true, true));
            imageView.setOnMouseClicked((MouseEvent e) -> {
                selectedGifUrl = url;
                if (listener != null) {
                    listener.onGifSelected(url);
                }
                imageView.getScene().getWindow().hide();
            });
            gifFlowPane.getChildren().add(imageView);
        }
    }

    public interface GifSelectionListener {
        void onGifSelected(String gifUrl);
    }
}
