package moe.caa.multilogin.core.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import moe.caa.multilogin.api.MultiLoginAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.core.auth.AuthCore;
import moe.caa.multilogin.core.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.yggdrasil.response.Property;
import moe.caa.multilogin.core.auth.yggdrasil.response.serialize.HasJoinedResponseSerializer;
import moe.caa.multilogin.core.auth.yggdrasil.response.serialize.PropertySerializer;
import moe.caa.multilogin.core.config.PluginConfig;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.core.database.SQLManager;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.language.LanguageHandler;
import moe.caa.multilogin.logger.Logger;
import moe.caa.multilogin.logger.LoggerLoadFailedException;
import moe.caa.multilogin.logger.MultiLoginLogger;

import java.io.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class MultiCore implements MultiLoginAPI {

    @Getter
    private static MultiCore instance;
    @Getter
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(HasJoinedResponse.class, new HasJoinedResponseSerializer())
            .registerTypeAdapter(Property.class, new PropertySerializer()).create();
    @Getter
    private final IPlugin plugin;

    @Getter
    private final SQLManager sqlManager;

    @Getter
    private final AuthCore authCore;

    @Getter
    private HttpClient httpClient;

    @Getter
    private PluginConfig config;

    public MultiCore(IPlugin plugin) throws LoggerLoadFailedException, IOException {
        if (instance != null) {
            throw new UnsupportedOperationException("Repeated");
        }
        MultiCore.instance = this;
        this.plugin = plugin;
        this.sqlManager = new SQLManager(this);
        this.authCore = new AuthCore(this);
        MultiLoginLogger.getInstance().init();
        LanguageHandler.getInstance().init("message.properties");
    }

    @Override
    public void onEnabled() throws IOException, ClassNotFoundException, SQLException {
        reload();
        sqlManager.init();
    }

    @Override
    public void onDisabled() {
        sqlManager.close();
        plugin.getRunServer().getScheduler().shutdown();
        MultiLoginLogger.getInstance().terminate();
        BaseFlows.close();
        plugin.getRunServer().shutdown();
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

        httpClient = HttpClient
                .newBuilder().connectTimeout(Duration.ofMillis(
                        MultiCore.getInstance().getConfig().getServicesTimeOut())
                ).build();
    }
}
