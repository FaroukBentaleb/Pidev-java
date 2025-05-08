package tn.learniverse.entities;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class BadWordsFilter {

    public static String filtrerTexte(String texte) {
        try {
            String encodedText = URLEncoder.encode(texte, StandardCharsets.UTF_8);
            String apiUrl = "https://www.purgomalum.com/service/plain?text=" + encodedText;


            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            return response.toString(); // Texte filtré
        } catch (Exception e) {
            e.printStackTrace();
            return texte; // Si erreur, retourne le texte d'origine
        }
    }
}

