package com.l33tfox.urai;

import com.l33tfox.urai.config.URAIConfig;
import com.l33tfox.urai.util.URAIUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

public class URAIClient implements ClientModInitializer {
	public static final String MOD_ID = "urai";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// global variable for timestamp in ms of last automated message sent
	public static Long lastMessageTime = null;
	// global variable for output from gemini
	public static String geminiResponse = null;
	// delay of 2 seconds between automated messages to avoid getting kicked for spam
	public static final long messageDelay = 2000;

	public static final String GEMINI_MODEL = "gemini-2.0-flash-lite";

	public static final HashSet<String> SUPPORTED_SERVER_DOMAINS = new HashSet<>();

	@Override
	public void onInitializeClient() {
		AutoConfig.register(URAIConfig.class, GsonConfigSerializer::new);
		URAIConfig config = AutoConfig.getConfigHolder(URAIConfig.class).getConfig();

		initValidServerDomains(config);

		AutoConfig.getConfigHolder(URAIConfig.class)
				.registerSaveListener((holder, savedConfig) -> {
					refreshUsingConfig(savedConfig);
					return ActionResult.SUCCESS;
				});

		// every tick, check for gemini response and send it as a chat message if it exists
		ClientTickEvents.END_CLIENT_TICK.register((client -> {
			if (!AutoConfig.getConfigHolder(URAIConfig.class).getConfig().modEnabled) {
				return;
			}

			if (client.player == null || URAIClient.geminiResponse == null ||
					(URAIClient.lastMessageTime != null && System.currentTimeMillis() - URAIClient.lastMessageTime <= URAIClient.messageDelay)) {
				return;
			}

			URAIClient.LOGGER.info("End tick Gemini response: {}", URAIClient.geminiResponse);

			// if player is on a supported server
			if (URAIUtils.isSupportedServer(client.player.networkHandler.getServerInfo())) {
				String sanitizedResponse = URAIUtils.sanitizeForMinecraftChat(URAIClient.geminiResponse);
				client.player.networkHandler.sendChatMessage(sanitizedResponse);
				URAIClient.LOGGER.info("sending sanitized chat {}", sanitizedResponse);
			}

			// reset gemini response and last message time to avoid spamming messages
			URAIClient.geminiResponse = null;
			URAIClient.lastMessageTime = System.currentTimeMillis();
		}));
	}

	private static void initValidServerDomains(URAIConfig config) {
		SUPPORTED_SERVER_DOMAINS.clear();

		for (String domain : config.supportedServerDomains) {
			SUPPORTED_SERVER_DOMAINS.add(domain);
		}
	}

	public static void refreshUsingConfig(URAIConfig config) {
		initValidServerDomains(config);
	}
}