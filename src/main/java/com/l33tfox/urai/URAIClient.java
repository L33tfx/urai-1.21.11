package com.l33tfox.urai;

import com.l33tfox.urai.config.URAIConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

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