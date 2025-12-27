package com.l33tfox.urai.util;

import com.l33tfox.urai.URAIClient;
import com.l33tfox.urai.config.URAIConfig;
import com.mojang.authlib.GameProfile;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;

import java.util.UUID;

public class URAIUtils {

    // Verify that the server the player is connected to is listed as supported in config
    public static boolean isSupportedServer(ServerInfo serverInfo) {
        if (serverInfo == null) {
            return false;
        }

        String serverAddress = serverInfo.address.toLowerCase();

        for (String serverDomain : URAIClient.SUPPORTED_SERVER_DOMAINS) {
            if (serverAddress.contains(serverDomain)) {
                return true;
            }
        }

        return false;
    }

    // Check if a chat message from another player starts with "Hey Gemini", meaning it should be fed into Gemini LLM
    public static boolean isMessageForGemini(SignedMessage message) {
        String messageContent = message.getSignedContent();
        String prefix = AutoConfig.getConfigHolder(URAIConfig.class).getConfig().geminiRequestStart.toLowerCase();
        return (messageContent.toLowerCase().startsWith(prefix));
    }

    public static String sanitizeForMinecraftChat(String text) {
        if (text == null) {
            return "";
        }

        String sanitized = text.replaceAll("[^\\x20-\\x7E—]", ""); // regex to match with all non-ascii characters nor em dashes

        if (sanitized.length() > 256) {
            sanitized = sanitized.substring(0, 256);
        }

        return sanitized;
    }
}
