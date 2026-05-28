package com.first;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Feature 1: Gemini AI Packet Explanation
 * Sends packet data to Google Gemini API and returns an explanation.
 * Uses REST API via HttpURLConnection (no external HTTP client needed).
 */
public class GeminiExplainer {

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent";

    private static final String SYSTEM_PROMPT =
            "You are a network security expert. Explain this network packet in simple English, " +
            "identify the protocol, source/destination, and flag anything suspicious: ";

    /**
     * Send packet data to Gemini API and get an explanation.
     * This method performs a blocking HTTP call — run it in a background thread.
     *
     * @param packetData The packet header + payload text to explain
     * @return The AI-generated explanation
     * @throws IOException if the API call fails
     */
    public static String explainPacket(String packetData) throws IOException {
        String apiKey = ConfigManager.getGeminiApiKey();
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
            throw new IOException("Gemini API key not configured. Please set 'gemini.api.key' in config.properties");
        }

        String fullPrompt = SYSTEM_PROMPT + packetData;

        // Build JSON request body
        String requestBody = buildRequestBody(fullPrompt);

        // Make HTTP request
        URL url = new URL(GEMINI_API_URL + "?key=" + apiKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);

        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        // Read response
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            String errorBody = readStream(connection.getErrorStream());
            throw new IOException("Gemini API returned HTTP " + responseCode + ": " + errorBody);
        }

        String responseBody = readStream(connection.getInputStream());
        return parseResponse(responseBody);
    }

    /**
     * Build the JSON request body for the Gemini API.
     * Manual JSON construction to avoid external JSON library dependency for this feature.
     */
    private static String buildRequestBody(String prompt) {
        // Escape special characters for JSON
        String escaped = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "{\"contents\":[{\"parts\":[{\"text\":\"" + escaped + "\"}]}],"
                + "\"generationConfig\":{\"temperature\":0.4,\"maxOutputTokens\":2048}}";
    }

    /**
     * Parse the Gemini API response to extract the generated text.
     */
    private static String parseResponse(String responseBody) {
        // Simple JSON parsing to extract the text field from candidates[0].content.parts[0].text
        // This avoids requiring an external JSON library just for this single parse
        int textStart = responseBody.indexOf("\"text\"");
        if (textStart == -1) {
            return "Error: Could not parse Gemini response. Raw response:\n" + responseBody;
        }

        // Find the value after "text": "
        int valueStart = responseBody.indexOf("\"", textStart + 6) + 1;
        if (valueStart == 0) {
            return "Error: Malformed response from Gemini API.";
        }

        // Find the closing quote (handling escaped quotes)
        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        for (int idx = valueStart; idx < responseBody.length(); idx++) {
            char c = responseBody.charAt(idx);
            if (escaped) {
                switch (c) {
                    case 'n': result.append('\n'); break;
                    case 'r': result.append('\r'); break;
                    case 't': result.append('\t'); break;
                    case '"': result.append('"'); break;
                    case '\\': result.append('\\'); break;
                    default: result.append('\\').append(c); break;
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Read an InputStream into a String.
     */
    private static String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        }
    }
}
