package tn.learniverse.services;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FlaskClient {

    private static final String API_URL = "http://127.0.0.1:5000/predict";
    public static String getSuggestion(String reclamationText, String partialResponse) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String jsonBody = String.format(
                    "{\"reclamation\": \"%s\", \"partial_response\": \"%s\"}",
                    reclamationText.replace("\"", "\\\""),
                    partialResponse.replace("\"", "\\\"")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:5000/predict"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            return jsonObject.get("suggestion").getAsString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la récupération de la suggestion.";
        }
    }

}
