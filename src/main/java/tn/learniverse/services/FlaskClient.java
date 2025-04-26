package tn.learniverse.services;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;

public class FlaskClient {

    private static final String API_SUGGESTION_URL = "http://127.0.0.1:5000/predict";
    private static final String API_BADWORDS_URL = "http://127.0.0.1:5000/check-bad-words";

    private static JsonElement parseJson(String jsonString) {
        try {
            JsonReader reader = new JsonReader(new StringReader(jsonString));
            reader.setLenient(true);
            return JsonParser.parseReader(reader);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getSuggestion(String reclamationText, String partialResponse) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String jsonBody = String.format(
                    "{\"reclamation\": \"%s\", \"partial_response\": \"%s\"}",
                    reclamationText.replace("\"", "\\\""),
                    partialResponse.replace("\"", "\\\"")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_SUGGESTION_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = parseJson(response.body());
            if (jsonElement != null && jsonElement.isJsonObject()) {
                return jsonElement.getAsJsonObject().get("suggestion").getAsString();
            }
            return "Erreur lors de la récupération de la suggestion.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la récupération de la suggestion.";
        }
    }

    public static boolean containsBadWords(String text) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String jsonBody = String.format(
                    "{\"text\": \"%s\"}",
                    text.replace("\"", "\\\"")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BADWORDS_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Réponse du serveur: " + response.body());

            JsonElement jsonElement = parseJson(response.body());
            if (jsonElement != null && jsonElement.isJsonObject()) {
                return jsonElement.getAsJsonObject().get("contains_bad_words").getAsBoolean();
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
