package moe.caa.multilogin.core.main;

import lombok.Getter;
import moe.caa.multilogin.api.MultiLoginAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.core.config.PluginConfig;
import moe.caa.multilogin.language.LanguageHandler;

public class MultiCore implements MultiLoginAPI {
    @Getter
    private final IPlugin plugin;
    @Getter
    private final LanguageHandler languageHandler;
    @Getter
    private PluginConfig config;

    public MultiCore(IPlugin plugin) {
        this.plugin = plugin;
        languageHandler = new LanguageHandler();

    }
}
