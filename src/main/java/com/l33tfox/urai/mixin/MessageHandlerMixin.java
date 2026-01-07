package com.l33tfox.urai.mixin;

import com.l33tfox.urai.URAIClient;
import com.l33tfox.urai.config.URAIConfig;
import com.l33tfox.urai.util.URAIUtils;
import com.mojang.authlib.GameProfile;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.l33tfox.urai.gemini.GeminiApiIntegration.getGeminiResponseAsync;

@Mixin(MessageHandler.class)
public abstract class MessageHandlerMixin {
	@Shadow @Final private MinecraftClient client;

	@Inject(at = @At("HEAD"), method = "onChatMessage")
	private void detectChatMessagesForGemini(SignedMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
		URAIClient.LOGGER.info("in chat message handler");
		detectMessageShared(message.getSignedContent());
	}

	@Inject(at = @At("HEAD"), method = "onGameMessage")
	private void detectChatMessagesForGemini(Text message, boolean overlay, CallbackInfo ci) {
		URAIClient.LOGGER.info("in game message handler");
		detectMessageShared(message.getString());
	}

	@Unique
	private void detectMessageShared(String messageContent) {
		ServerInfo serverInfo = client.getCurrentServerEntry();
		URAIConfig config = AutoConfig.getConfigHolder(URAIConfig.class).getConfig();

		if ((!config.enableOnAllServers && !URAIUtils.isSupportedServer(serverInfo)) || !config.modEnabled
			|| URAIClient.geminiRequestInProgress) {
			return;
		}

		// if an auto message has been sent recently, do nothing
		if (URAIClient.lastMessageTime != null && System.currentTimeMillis() - URAIClient.lastMessageTime <= config.messageDelay) {
			return;
		}

		if (URAIUtils.isMessageForGemini(messageContent)) {
			URAIClient.LOGGER.info("is message");
			String prefix = AutoConfig.getConfigHolder(URAIConfig.class).getConfig().geminiRequestStart.toLowerCase();
			String request = messageContent.toLowerCase().split(prefix, 2)[1].trim();

			URAIClient.geminiRequestInProgress = true;

			// set geminiResponse after async CompletableFuture<String> is resolved
			getGeminiResponseAsync(request).thenAccept(responseText -> {
				URAIClient.geminiResponse = responseText;
				URAIClient.LOGGER.info("gemini response: {}", responseText);
			})
			.exceptionally(e -> {
				URAIClient.LOGGER.info("Gemini async request failed: {}", e.getMessage());
				return null;
			})
			.whenComplete((result, throwable) -> {
				URAIClient.geminiRequestInProgress = false;
			});
		}
	}
}