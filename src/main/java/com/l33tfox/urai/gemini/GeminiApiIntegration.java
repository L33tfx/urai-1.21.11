package com.l33tfox.urai.gemini;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.l33tfox.urai.URAIClient;
import com.l33tfox.urai.config.URAIConfig;
import me.shedaniel.autoconfig.AutoConfig;

import javax.annotation.Nullable;

public class GeminiApiIntegration {

    @Nullable
    public static String getGeminiResponse(String request) {
        URAIConfig config = AutoConfig.getConfigHolder(URAIConfig.class).getConfig();

        GenerateContentConfig gcConfig = GenerateContentConfig.builder()
                .systemInstruction(
                    Content.fromParts(
                        Part.fromText(AutoConfig.getConfigHolder(URAIConfig.class).getConfig().geminiContextMessage)
                    )
                )
                .maxOutputTokens(128)
                .build();

        if (!config.modEnabled) {
            return null; // this should never happen due to other modEnabled checks, but here for safety
        }

        String apiKey = config.geminiApiKey;
        Client client = Client.builder().apiKey(apiKey).build();
        GenerateContentResponse response = null;

        try {
            response = client.models.generateContent("gemini-2.0-flash-lite", request, gcConfig);
        } catch (Exception e) {
            URAIClient.LOGGER.info("Something went wrong: {}", e.toString());
        }

        if (response == null) {
            return null;
        }

        URAIClient.LOGGER.info("Gemini unsanitized response: {}", response.text());
        return response.text();
    }
}
