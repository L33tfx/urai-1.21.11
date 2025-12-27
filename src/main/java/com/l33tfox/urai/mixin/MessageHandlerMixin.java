package com.l33tfox.urai.mixin;

import com.l33tfox.urai.URAIClient;
import com.l33tfox.urai.config.URAIConfig;
import com.l33tfox.urai.gemini.GeminiApiIntegration;
import com.l33tfox.urai.util.URAIUtils;
import com.mojang.authlib.GameProfile;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageHandler.class)
public abstract class MessageHandlerMixin {
	@Shadow @Final private MinecraftClient client;

	@Inject(at = @At("HEAD"), method = "onChatMessage")
	private void detectChatMessagesForGemini(SignedMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
		ServerInfo serverInfo = client.getCurrentServerEntry();

		if (!URAIUtils.isSupportedServer(serverInfo) || !AutoConfig.getConfigHolder(URAIConfig.class).getConfig().modEnabled) {
			return;
		}

		// if an auto message has been sent recently, do nothing
		if (URAIClient.lastMessageTime != null && System.currentTimeMillis() - URAIClient.lastMessageTime <= URAIClient.messageDelay) {
			return;
		}

		if (URAIUtils.isMessageForGemini(message)) {
			URAIClient.geminiResponse = GeminiApiIntegration.getGeminiResponse(message.getSignedContent().substring("hey gemini".length()).trim());
		}
	}
}