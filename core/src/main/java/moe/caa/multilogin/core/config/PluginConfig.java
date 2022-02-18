package moe.caa.multilogin.core.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.File;
import java.util.*;

/**
 * 插件配置文件
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PluginConfig {
    private List<YggdrasilService> yggdrasilServices;
    private int servicesTimeOut;
    private int skinRestorerRetryDelay;
    private boolean globalWhitelist;

    private SQLBackendType sqlBackend;
    private String sqlIp;
    private int sqlPort;
    private String sqlUsername;
    private String sqlPassword;
    private String sqlDatabase;
    private String sqlTablePrefix;

    /**
     * 插件配置读取
     */
    public static PluginConfig reload(File file) throws ConfigurateException {
        PluginConfig config = new PluginConfig();
        CommentedConfigurationNode conf = HoconConfigurationLoader.builder()
                .file(file).build().load();

        // 读 Yggdrasil 列表
        final CommentedConfigurationNode services = conf.node("yggdrasilServices");
        config.yggdrasilServices = new ArrayList<>();
        Set<Integer> temp = new HashSet<>();
        for (CommentedConfigurationNode node : services.childrenMap().values()) {
            final YggdrasilService e = YggdrasilService.parseConfig(node);
            if (!temp.add(e.getId())) throw new IllegalArgumentException("Duplicate yggdrasil ID: " + e.getId());
            config.yggdrasilServices.add(e);
        }
        config.yggdrasilServices = Collections.unmodifiableList(config.yggdrasilServices);


        // 读其他配置
        config.servicesTimeOut = conf.node("servicesTimeOut").getInt(10000);
        config.skinRestorerRetryDelay = conf.node("skinRestorerRetryDelay").getInt(5000);
        config.globalWhitelist = conf.node("globalWhitelist").getBoolean(true);
        config.globalWhitelist = conf.node("strictMode").getBoolean(true);

        // 读数据库配置
        final CommentedConfigurationNode sql = conf.node("sql");
        config.sqlBackend = sql.node("backend").get(SQLBackendType.class, SQLBackendType.H2);
        config.sqlIp = sql.node("ip").getString("127.0.0.1");
        config.sqlPort = sql.node("port").getInt(3306);
        config.sqlUsername = sql.node("username").getString("root");
        config.sqlPassword = sql.node("password").getString("12345");
        config.sqlDatabase = sql.node("database").getString("multilogin");
        config.sqlTablePrefix = sql.node("tablePrefix").getString("multilogin");
        return config;
    }
}
