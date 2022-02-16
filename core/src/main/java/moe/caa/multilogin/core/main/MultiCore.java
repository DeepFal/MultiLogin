package moe.caa.multilogin.core.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import moe.caa.multilogin.api.MultiLoginAPI;
import moe.caa.multilogin.api.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.api.auth.yggdrasil.response.Property;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.core.auth.AuthCore;
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

    /**
     * 检查条件
     *
     * @return 是否启用
     */
    private boolean checkCondition() {
        if (plugin.getRunServer().isOnlineMode()) {
            Logger.LoggerProvider.getLogger().warn("The server is running in offline mode, in which the plugin does not work at all !!!");
            return false;
        }
        if (!plugin.getRunServer().isForwarded()) {
            Logger.LoggerProvider.getLogger()
                    .warn("No forwarding will be done, All player UUID's will be out of control, with serious consequence !!!");
            return false;
        }
        if (!plugin.getRunServer().isWhitelist()) {
            Logger.LoggerProvider.getLogger()
                    .warn("The vanilla whitelist does not work with this multi-authentication system. Turn it off.");
        }
        return true;
    }

    @Override
    public void onEnable() {
        try {
            reload();
            sqlManager.init();
            if (!checkCondition()) {
                onDisabled();
                return;
            }
            Logger.LoggerProvider.getLogger().info("Plugin enabled.");
        } catch (Throwable throwable) {
            Logger.LoggerProvider.getLogger().error(
                    "A fatal error occurred when the plugin was enabled, shutting down the server.", throwable);
            onDisabled();
        }
    }

    @Override
    public void onDisabled() {
        try {
            // 首先关闭所有线程池确保服务端能正常关闭
            plugin.getRunServer().getScheduler().shutdown();
            BaseFlows.close();

            // 关闭 Log4J
            MultiLoginLogger.getInstance().terminate();

            // 关闭服务端
            plugin.getRunServer().shutdown();

            // 关闭数据库（故障率最高，挪到最后）
            sqlManager.close();
        } catch (Throwable throwable) {
            Logger.LoggerProvider.getLogger().info("An exception occurred while the plugin was disabled.", throwable);
        } finally {
            Logger.LoggerProvider.getLogger().info("Plugin disabled.");
        }
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
