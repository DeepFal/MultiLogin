package moe.caa.multilogin.core.main;

import lombok.Getter;
import moe.caa.multilogin.api.MultiLoginAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.core.config.PluginConfig;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.core.database.SQLManager;
import moe.caa.multilogin.language.LanguageHandler;
import moe.caa.multilogin.logger.Logger;
import moe.caa.multilogin.logger.LoggerLoadFailedException;
import moe.caa.multilogin.logger.MultiLoginLogger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class MultiCore implements MultiLoginAPI {
    @Getter
    private final IPlugin plugin;

    @Getter
    private final SQLManager sqlManager;

    @Getter
    private PluginConfig config;

    public MultiCore(IPlugin plugin) throws LoggerLoadFailedException, IOException {
        this.plugin = plugin;
        this.sqlManager = new SQLManager(this);
        MultiLoginLogger.getInstance().init();
        LanguageHandler.getInstance().init("message.properties");
    }

    @Override
    public void init() throws IOException, ClassNotFoundException, SQLException {
        reload();
        sqlManager.init();
    }


    @Override
    public synchronized void reload() throws IOException {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            throw new IOException(String.format("Unable to create folder: %s", plugin.getDataFolder().getAbsolutePath()));
        }
        final File file = new File(plugin.getDataFolder(), "config.conf");
        if (!file.exists()) {
            try (LineNumberReader reader = new LineNumberReader(
                    new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/config.conf")), StandardCharsets.UTF_8));
                 BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(
                         new FileOutputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    fw.write(line);
                    fw.write('\n');
                }
                fw.flush();
            }
        }

        config = PluginConfig.reload(file);
        final List<YggdrasilService> yggdrasilServices = config.getYggdrasilServices();
        int countAll = yggdrasilServices.size();
        int enabled = (int) yggdrasilServices.stream().filter(YggdrasilService::isEnable).count();
        Logger.LoggerProvider.getLogger().info(String.format("%d Yggdrasil Service is added, of which %d is enabled.", countAll, enabled));
        if (enabled == 0)
            Logger.LoggerProvider.getLogger().warn("Without any yggdrasil service enabled, all players will not be able to log in to the game");
    }
}
