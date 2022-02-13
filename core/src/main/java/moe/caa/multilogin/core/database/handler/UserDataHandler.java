package moe.caa.multilogin.core.database.handler;

import moe.caa.multilogin.core.database.SQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDataHandler {
    private final SQLManager sqlManager;

    private final String fieldOnlineUuid = "online_uuid";
    private final String fieldYggdrasilPath = "yggdrasil_path";
    private final String fieldInGameUuid = "in_game_uuid";
    private final String fieldCurrentUsername = "current_username";
    private final String fieldWhitelist = "whitelist";

    /**
     * 构建这个玩家数据管理类
     *
     * @param sqlManager 数据库管理类
     */
    public UserDataHandler(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
    }

    public void init() throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + SQLManager.getUserDataTableName() + "( " +
                     fieldOnlineUuid + " BINARY(16) NOT NULL, " +
                     fieldYggdrasilPath + "VARCHAR(100) NOT NULL, " +
                     fieldInGameUuid + " BINARY(16) NOT NULL, " +
                     fieldCurrentUsername + " VARCHAR(100) NOT NULL, " +
                     fieldWhitelist + " BOOL, " +
                     "PRIMARY KEY(" + fieldOnlineUuid + ", " + fieldYggdrasilPath + ")"
             )) {
            preparedStatement.executeUpdate();
        }
    }
}
