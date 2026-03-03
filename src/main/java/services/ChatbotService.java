package services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ChatbotService {

    // ────────────────────────────────────────────────
    // Configuration Groq (2026)
    // ────────────────────────────────────────────────
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    private static final String API_KEY  = "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";

    // Modèles très rapides et performants chez Groq en 2026 (choisis-en un)
    private static final String MODEL    = "llama-3.3-70b-versatile";
    // Alternatives populaires :
    // "mixtral-8x7b-32768" (très bon équilibre)
    // "llama-3.1-70b-versatile" (classique rapide)
    // "gemma2-9b-it" (plus léger et rapide)

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public String sendMessage(String userMessage) {
        try {
            JsonArray messages = new JsonArray();

            // Prompt système (agricole, en français)
            JsonObject systemMsg = new JsonObject();
            systemMsg.addProperty("role", "system");
            systemMsg.addProperty("content",
                    "Tu es un assistant agricole intelligent pour Smart Farm. " +
                            "Réponds de manière concise, utile et en français. " +
                            "Utilise des emojis 🌾🐄🚜 quand c'est pertinent.");
            messages.add(systemMsg);

            // Message utilisateur
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", userMessage);
            messages.add(userMsg);

            // Corps de la requête
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL);
            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 500);

            String jsonBody = gson.toJson(requestBody);

            // Requête HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String body = response.body();

            if (statusCode >= 200 && statusCode < 300) {
                JsonObject jsonResponse = gson.fromJson(body, JsonObject.class);
                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices != null && choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    JsonObject messageObj = firstChoice.getAsJsonObject("message");
                    return messageObj.get("content").getAsString().trim();
                }
                return "Réponse reçue mais format inattendu.";
            } else {
                // Meilleur message d'erreur pour le debug
                String errorPreview = body.length() > 180 ? body.substring(0, 180) + "..." : body;
                return "Erreur Groq API " + statusCode + " → " + errorPreview +
                        "\nVérifie ta clé API sur https://console.groq.com/keys";
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Erreur de connexion : " + e.getMessage();
        } catch (Exception e) {
            return "Erreur inattendue : " + e.getClass().getSimpleName() + " → " + e.getMessage();
        }
    }
}