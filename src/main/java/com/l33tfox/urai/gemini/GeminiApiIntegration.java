package com.l33tfox.urai.gemini;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.l33tfox.urai.URAIClient;
import com.l33tfox.urai.config.URAIConfig;
import me.shedaniel.autoconfig.AutoConfig;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GeminiApiIntegration {

    // Asynchronously make a POST request to Gemini API and return Gemini's text response as a CompletableFuture
    public static CompletableFuture<String> getGeminiResponseAsync(String request) {
        URAIConfig config = AutoConfig.getConfigHolder(URAIConfig.class).getConfig();

        if (!config.modEnabled) {
            return null; // this should never happen due to other modEnabled checks, but here for safety
        }

        String geminiApiEndpoint = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s", config.geminiModel, config.geminiApiKey);

        String jsonRequest = String.format(
                """
                {
                    "systemInstruction": {
                        "parts": [
                            { "text": "%s" }
                        ]
                    },
                    "contents": [
                        {
                            "parts": [
                                { "text": "%s" }
                            ]
                        }
                    ],
                    "generationConfig": {
                        "maxOutputTokens": %d
                    }
                }
                """,
                escapeJson(config.geminiContextMessage), escapeJson(request), config.maxOutputTokens);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest httpRequest = HttpRequest
                .newBuilder()
                .uri(URI.create(geminiApiEndpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        URAIClient.LOGGER.info("json request: {}", jsonRequest);

        CompletableFuture<HttpResponse<String>> responseFuture = client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());

        // return CompletableFuture<String> which holds Gemini's text response
        return responseFuture
                .thenApply(HttpResponse::body)
                .thenApply(responseBody -> { // parse Json response for Gemini's text response
                    JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
                    return root
                            .getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                });
    }

    // Escape backslashes and quotes to preserve valid json formatting
    private static String escapeJson(String str) {
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
