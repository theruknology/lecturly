package org.example.lecturly;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;

/**
 * AudioProcessingService acts as a client for the FastAPI backend.
 * The FastAPI server (audio_backend/app.py) handles:
 * - Audio file upload to Gemini Files API
 * - Note generation using Gemini API
 *
 * This keeps the Java code clean and delegates audio processing to Python,
 * which handles REST API calls more reliably.
 */
public class AudioProcessingService {
    private HttpClient httpClient;
    private Gson gson;
    private static final String FASTAPI_BACKEND = "http://localhost:8000";

    public AudioProcessingService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    /**
     * Send audio file to FastAPI backend for processing
     * @param audioFile Audio file to process
     * @return Generated lecture notes in markdown format
     * @throws Exception if processing fails
     */
    public String generateNotesFromAudio(File audioFile) throws Exception {
        if (!audioFile.exists()) {
            throw new IllegalArgumentException("Audio file not found: " + audioFile.getAbsolutePath());
        }

        byte[] fileContent = Files.readAllBytes(audioFile.toPath());
        String boundary = "----FormBoundary" + System.currentTimeMillis();

        // Build multipart/form-data request
        byte[] requestBody = buildMultipartFormData(fileContent, audioFile.getName(), boundary);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(FASTAPI_BACKEND + "/audio-to-notes"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .timeout(java.time.Duration.ofMinutes(5))
                .build();

        System.out.println("Sending audio file to FastAPI backend: " + audioFile.getName());

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.err.println("FastAPI Error (" + response.statusCode() + "): " + response.body());
                throw new Exception("FastAPI backend error: " + response.body());
            }

            JsonObject result = gson.fromJson(response.body(), JsonObject.class);
            
            if (!result.has("notes")) {
                throw new Exception("No notes in response");
            }

            String notes = result.get("notes").getAsString();
            System.out.println("Successfully generated notes from audio");
            return notes;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Exception("Request interrupted: " + e.getMessage());
        }
    }

    /**
     * Build multipart/form-data request body
     */
    private byte[] buildMultipartFormData(byte[] fileContent, String filename, String boundary) throws Exception {
        StringBuilder sb = new StringBuilder();

        // Add file part
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(filename).append("\"\r\n");
        sb.append("Content-Type: ").append(getMimeType(filename)).append("\r\n");
        sb.append("\r\n");

        byte[] header = sb.toString().getBytes();
        byte[] footer = ("\r\n--" + boundary + "--\r\n").getBytes();

        byte[] result = new byte[header.length + fileContent.length + footer.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(fileContent, 0, result, header.length, fileContent.length);
        System.arraycopy(footer, 0, result, header.length + fileContent.length, footer.length);

        return result;
    }

    private String getMimeType(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (ext) {
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            case "flac" -> "audio/flac";
            case "m4a" -> "audio/mp4";
            case "aac" -> "audio/aac";
            case "aiff" -> "audio/aiff";
            default -> "audio/mpeg";
        };
    }

    /**
     * Check if FastAPI backend is available
     * @return true if backend is accessible
     */
    public boolean isBackendAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(FASTAPI_BACKEND + "/health"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("FastAPI backend not available: " + e.getMessage());
            return false;
        }
    }
}

