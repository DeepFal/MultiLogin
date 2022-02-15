package moe.caa.multilogin.core.database;

import lombok.Getter;
import moe.caa.multilogin.core.config.SQLBackendType;
import moe.caa.multilogin.core.database.handler.CacheWhitelistDataHandler;
import moe.caa.multilogin.core.database.handler.SkinRestorerDataHandler;
import moe.caa.multilogin.core.database.handler.UserDataHandler;
import moe.caa.multilogin.core.database.pool.H2ConnectionPool;
import moe.caa.multilogin.core.database.pool.ISQLConnectionPool;
import moe.caa.multilogin.core.database.pool.MysqlConnectionPool;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.logger.Logger;

import java.sql.SQLException;

/**
 * 数据库管理类
 */
public class SQLManager {

    @Getter
    private static String userDataTableName = "{0}user_data_v2";

    @Getter
    private static String cacheWhitelistTableName = "{0}cache_whitelist_v2";

    @Getter
    private static String skinRestorerTableName = "{0}skin_restorer_v2";

    @Getter
    private final MultiCore core;
    @Getter
    private final UserDataHandler userDataHandler = new UserDataHandler(this);
    @Getter
    private final SkinRestorerDataHandler skinRestorerDataHandler = new SkinRestorerDataHandler(this);
    @Getter
    private final CacheWhitelistDataHandler cacheWhitelistDataHandler = new CacheWhitelistDataHandler(this);
    @Getter
    private ISQLConnectionPool pool;

    public SQLManager(MultiCore core) {
        this.core = core;
    }

    /**
     * 初始化和链接数据库
     */
    public void init() throws ClassNotFoundException, SQLException {
        String prefix = core.getConfig().getSqlTablePrefix();
        prefix = prefix.trim().length() != 0 ? prefix + "_" : "";

        userDataTableName = userDataTableName.replace("{0}", prefix);
        cacheWhitelistTableName = cacheWhitelistTableName.replace("{0}", prefix);
        skinRestorerTableName = skinRestorerTableName.replace("{0}", prefix);

        if (core.getConfig().getSqlBackend() == SQLBackendType.MYSQL) {
            pool = new MysqlConnectionPool(
                    core.getConfig().getSqlIp(),
                    core.getConfig().getSqlPort(),
                    core.getConfig().getSqlDatabase(),
                    core.getConfig().getSqlUsername(),
                    core.getConfig().getSqlPassword()
            );
        } else {
            pool = new H2ConnectionPool(core.getPlugin().getDataFolder().getAbsolutePath() + "/multilogin",
                    core.getConfig().getSqlUsername(),
                    core.getConfig().getSqlPassword()
            );
        }
        Logger.LoggerProvider.getLogger().info(String.format("Using %s database", pool.name()));

        userDataHandler.init();
        skinRestorerDataHandler.init();
        cacheWhitelistDataHandler.init();
    }

    public void close() {
        if (pool != null) pool.close();
    }
}
