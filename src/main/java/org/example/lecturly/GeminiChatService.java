package org.example.lecturly;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class GeminiChatService {
    private String apiKey;
    private HttpClient httpClient;
    private Gson gson;
    private List<JsonObject> conversationHistory;
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    public GeminiChatService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.conversationHistory = new ArrayList<>();
    }

    public String chat(String userMessage) throws Exception {
        // Add user message to history
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray userParts = new JsonArray();
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", userMessage);
        userParts.add(userPart);
        userContent.add("parts", userParts);
        conversationHistory.add(userContent);

        try {
            // Create request body
            JsonObject requestBody = new JsonObject();
            JsonArray contentsArray = new JsonArray();
            for (JsonObject content : conversationHistory) {
                contentsArray.add(content);
            }
            requestBody.add("contents", contentsArray);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(API_URL + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new Exception("API Error " + response.statusCode() + ": " + response.body());
            }

            JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);

            // Extract text from response
            String assistantResponse = extractTextFromResponse(responseJson);

            // Add assistant response to history
            JsonObject assistantContent = new JsonObject();
            assistantContent.addProperty("role", "model");
            JsonArray assistantParts = new JsonArray();
            JsonObject assistantPart = new JsonObject();
            assistantPart.addProperty("text", assistantResponse);
            assistantParts.add(assistantPart);
            assistantContent.add("parts", assistantParts);
            conversationHistory.add(assistantContent);

            return assistantResponse;
        } catch (Exception e) {
            // Remove the user message if API call fails
            conversationHistory.remove(conversationHistory.size() - 1);
            throw e;
        }
    }

    private String extractTextFromResponse(JsonObject responseJson) {
        try {
            if (responseJson.has("candidates")) {
                JsonArray candidates = responseJson.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject candidate = candidates.get(0).getAsJsonObject();
                    if (candidate.has("content")) {
                        JsonObject content = candidate.getAsJsonObject("content");
                        if (content.has("parts")) {
                            JsonArray parts = content.getAsJsonArray("parts");
                            if (parts.size() > 0) {
                                JsonObject part = parts.get(0).getAsJsonObject();
                                if (part.has("text")) {
                                    return part.get("text").getAsString();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting text from response: " + e.getMessage());
        }
        return "Unable to parse response";
    }

    public void clearHistory() {
        conversationHistory.clear();
    }

    public List<JsonObject> getHistory() {
        return new ArrayList<>(conversationHistory);
    }

    /**
     * Restore conversation history from saved messages
     * Used when loading a notebook with existing chat history
     * @param messages The saved chat messages
     * @param notesContext Optional notes context to prepend to the first user message
     */
    public void restoreHistory(List<Notebook.ChatMessage> messages, String notesContext) {
        conversationHistory.clear();
        boolean isFirstUserMessage = true;
        
        for (Notebook.ChatMessage msg : messages) {
            JsonObject content = new JsonObject();
            content.addProperty("role", msg.getRole().equals("user") ? "user" : "model");
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            
            String messageText = msg.getContent();
            // For the first user message, add notes context if available
            if (msg.getRole().equals("user") && isFirstUserMessage && notesContext != null && !notesContext.trim().isEmpty()) {
                messageText = "Here are my notes:\n\n" + notesContext + "\n\nNow, " + messageText;
                isFirstUserMessage = false;
            } else if (msg.getRole().equals("user")) {
                isFirstUserMessage = false;
            }
            
            part.addProperty("text", messageText);
            parts.add(part);
            content.add("parts", parts);
            conversationHistory.add(content);
        }
    }
}
