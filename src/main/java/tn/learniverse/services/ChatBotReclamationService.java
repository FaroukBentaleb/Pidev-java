package tn.learniverse.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChatBotReclamationService {

    private static final String API_KEY = "AIzaSyDHWOIeTrwZoxCDofoVZk2nh3HpLu0NFvQ";
    private static final String MODEL_NAME = "gemini-2.0-flash";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent?key=" + API_KEY;
    private final List<ChatMessage> conversationHistory = new ArrayList<>();

    public String envoyer(String messageUtilisateur) {
        try {
            conversationHistory.add(new ChatMessage("user", messageUtilisateur));

            JSONArray contentsArray = new JSONArray();

            if (conversationHistory.size() == 1) {
                contentsArray.put(new JSONObject()
                        .put("role", "user")
                        .put("parts", new JSONArray().put(new JSONObject()
                                .put("text", "Tu es un assistant spécialisé uniquement dans la gestion des réclamations client (paiement, livraison, produit endommagé, service client, etc.). Si une question n'est pas liée à une réclamation, répond : 'Désolé, je ne peux répondre qu'aux questions liées aux réclamations.'"))));
            }

            // Vérification si le message de l'utilisateur semble lié à un problème nécessitant une réclamation
            if (messageUtilisateur.contains("problème") || messageUtilisateur.contains("erreur") || messageUtilisateur.contains("réclamation")) {
                // Réponse de demande de réclamation
                String responseText = "Vous pouvez envoyer une réclamation contenant les informations suivantes pour résoudre ce problème :\n\n"
                        + "1. Votre nom complet et adresse e-mail utilisée lors de l'inscription/achat.\n"
                        + "2. Le nom précis du cours auquel vous essayez d'accéder.\n"
                        + "3. La date et l'heure du paiement.\n"
                        + "4. Une capture d'écran de la confirmation de paiement.\n"
                        + "5. Une capture d'écran du message d'erreur ou de ce qui se passe lorsque vous essayez d'accéder au cours.\n\n"
                        + "Voulez-vous que je vous fournisse le contenu de votre réclamation à envoyer ?";

                conversationHistory.add(new ChatMessage("model", responseText));
                return responseText;
            }

            // Construction de la requête pour l'API
            for (ChatMessage msg : conversationHistory) {
                JSONArray parts = new JSONArray().put(new JSONObject().put("text", msg.text));
                contentsArray.put(new JSONObject()
                        .put("role", msg.role)
                        .put("parts", parts));
            }

            JSONObject jsonRequest = new JSONObject().put("contents", contentsArray);

            URL url = new URL(API_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonRequest.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int responseCode = con.getResponseCode();
            System.out.println("Code de réponse API: " + responseCode);

            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }

                    JSONObject obj = new JSONObject(response.toString());
                    JSONArray candidates = obj.getJSONArray("candidates");
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    JSONArray parts = firstCandidate.getJSONObject("content").getJSONArray("parts");
                    String responseText = parts.getJSONObject(0).getString("text");

                    conversationHistory.add(new ChatMessage("model", responseText));
                    return responseText;
                }
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    System.out.println("Message d'erreur API: " + errorResponse);
                    return "Une erreur est survenue. Code: " + responseCode;
                }
            }

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return "Erreur lors de la communication avec Gemini.";
        }
    }

    private static class ChatMessage {
        String role;
        String text;

        ChatMessage(String role, String text) {
            this.role = role;
            this.text = text;
        }
    }
}
