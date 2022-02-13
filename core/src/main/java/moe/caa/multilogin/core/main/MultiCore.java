package moe.caa.multilogin.core.main;

import lombok.Getter;
import moe.caa.multilogin.api.MultiLoginAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.core.config.PluginConfig;
import moe.caa.multilogin.language.LanguageHandler;
import moe.caa.multilogin.logger.LoggerLoadFailedException;
import moe.caa.multilogin.logger.MultiLoginLogger;

import java.io.IOException;

public class MultiCore implements MultiLoginAPI {
    @Getter
    private final IPlugin plugin;

    @Getter
    private PluginConfig config;

    public MultiCore(IPlugin plugin) throws LoggerLoadFailedException, IOException {
        this.plugin = plugin;
        MultiLoginLogger.getInstance().init();
        LanguageHandler.getInstance().init("message.properties");
    }

    public void init() {

    }


    private void reload() {

    }
}
