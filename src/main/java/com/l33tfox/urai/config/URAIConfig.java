package com.l33tfox.urai.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

@Config(name = "urai")
public class URAIConfig implements ConfigData {
    public boolean modEnabled = true;

    public String geminiApiKey = "AIzaSyALFe9T_a9_7BNQY9NBQmoXOMJCcBpz4vc"; // TODO: delete key in portal later

    public int maxOutputTokens = 128;

    public String geminiRequestStart = "Hey Gemini";

    @ConfigEntry.Gui.Tooltip
    public String geminiContextMessage = "";

    @ConfigEntry.Gui.Tooltip
    public String gameStartIntroMessage = "Hello! I'm Gemini, your AI assistant. Want to chat? Just start your message with \"Hey Gemini\" followed by your question. To keep things fair and avoid spam, I can only reply once every 3 seconds. Good luck with the game!";

    public boolean enableOnAllServers = true;

    @ConfigEntry.Gui.Tooltip
    public ArrayList<String> supportedServerDomains = new ArrayList<>(List.of("hoplite.gg", "hypixel.net", "localhost"));
}
