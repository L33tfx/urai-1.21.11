package com.l33tfox.urai.mixin;

import com.l33tfox.urai.URAIClient;
import com.l33tfox.urai.config.URAIConfig;
import com.l33tfox.urai.constants.ServerDomains;
import com.l33tfox.urai.util.URAIUtils;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Random;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Shadow @Final
    MinecraftClient client;

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V")
    private void sendGeminiResponse(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci) {
        if (!AutoConfig.getConfigHolder(URAIConfig.class).getConfig().modEnabled) {
            return;
        }

        URAIClient.LOGGER.info("Chat HUD Gemini response: {}", URAIClient.geminiResponse);

        if (client == null || client.player == null || URAIClient.geminiResponse == null ||
                (URAIClient.lastMessageTime != null && System.currentTimeMillis() - URAIClient.lastMessageTime <= URAIClient.messageDelay)) {
            return;
        }

        // if player is on a supported server
        if (URAIUtils.isSupportedServer(client.player.networkHandler.getServerInfo())) {
            String sanitizedResponse = URAIUtils.sanitizeForMinecraftChat(URAIClient.geminiResponse);
            client.player.networkHandler.sendChatMessage(sanitizedResponse);
            URAIClient.LOGGER.info("sending sanitized chat {}", sanitizedResponse);
        }

        // reset gemini response and last message time to avoid spamming messages
        URAIClient.geminiResponse = null;
        URAIClient.lastMessageTime = System.currentTimeMillis();
    }
}
