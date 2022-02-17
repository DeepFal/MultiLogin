package moe.caa.multilogin.core.database.handler;

import moe.caa.multilogin.core.database.SQLManager;
import moe.caa.multilogin.core.util.ValueUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.UUID;

public class CacheWhitelistDataHandler {
    private static final String fieldCurrentUsername = "current_user_name";
    private static final String fieldOnlineUuid = "online_uuid";
    private static final String fieldYggdrasilId = "yggdrasil_id";
    private final SQLManager sqlManager;

    /**
     * 构建这个皮肤修复数据管理类
     *
     * @param sqlManager 数据库管理类
     */
    public CacheWhitelistDataHandler(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
    }

    public void init() throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + SQLManager.getCacheWhitelistTableName() + "(" +
                     fieldCurrentUsername + " VARCHAR(32), " +
                     fieldOnlineUuid + " BINARY(16), " +
                     fieldYggdrasilId + " TINYINT)"
             )) {
            preparedStatement.executeUpdate();
        }
    }

    /**
     * 插入一条记录
     */
    public void insertNew(String currentUsername) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("INSERT INTO %s (%s) VALUES(?)",
                             SQLManager.getCacheWhitelistTableName(), fieldCurrentUsername
                     ))) {
            preparedStatement.setString(1, currentUsername);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * 插入一条记录
     */
    public void insertNew(UUID onlineUuid) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("INSERT INTO %s (%s) VALUES(?)",
                             SQLManager.getCacheWhitelistTableName(), fieldOnlineUuid
                     ))) {
            preparedStatement.setBytes(1, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.executeUpdate();
        }
    }

    /**
     * 插入一条记录
     */
    public void insertNew(String currentUsername, UUID onlineUuid) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("INSERT INTO %s (%s, %s) VALUES(?, ?)",
                             SQLManager.getCacheWhitelistTableName(), fieldCurrentUsername,
                             fieldOnlineUuid
                     ))) {
            preparedStatement.setString(1, currentUsername);
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.executeUpdate();
        }
    }

    /**
     * 插入一条记录
     */
    public void insertNew(String currentUsername, UUID onlineUuid, int yggdrasilId) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("INSERT INTO %s (%s, %s, %s) VALUES(?, ?, ?, ?)",
                             SQLManager.getCacheWhitelistTableName(), fieldCurrentUsername,
                             fieldOnlineUuid, fieldYggdrasilId
                     ))) {
            preparedStatement.setString(1, currentUsername);
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(3, yggdrasilId);
            preparedStatement.executeUpdate();
        }
    }

    public boolean hasWhitelistAndRemove(String currentUsername, UUID onlineUuid, int yggdrasilId) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     MessageFormat.format(
                             "DELETE FROM {0} WHERE " +

                                     "({2} IS NULL AND {3} IS NULL AND {1} IS NOT NULL AND {1} = ?)" + " OR " +
                                     "({3} IS NULL AND {1} IS NULL AND {2} IS NOT NULL AND {2} = ?)" + " OR " +
                                     "({1} IS NULL AND {2} IS NULL AND {3} IS NOT NULL AND {3} = ?)" + " OR " +

                                     "({3} IS NULL AND {2} IS NOT NULL AND {1} IS NOT NULL AND {1} = ? AND {2} = ?)" + " OR " +
                                     "({1} IS NULL AND {2} IS NOT NULL AND {3} IS NOT NULL AND {2} = ? AND {3} = ?)" + " OR " +
                                     "({2} IS NULL AND {3} IS NOT NULL AND {1} IS NOT NULL AND {3} = ? AND {1} = ?)" + " OR " +

                                     "({3} IS NOT NULL AND {2} IS NOT NULL AND {1} IS NOT NULL AND {1} = ? AND {2} = ? AND {3} = ?)"
                             , SQLManager.getCacheWhitelistTableName(),
                             fieldYggdrasilId, fieldOnlineUuid, fieldCurrentUsername
                     ))) {
            preparedStatement.setInt(1, yggdrasilId);
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setString(3, currentUsername);

            preparedStatement.setInt(4, yggdrasilId);
            preparedStatement.setBytes(5, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setBytes(6, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setString(7, currentUsername);
            preparedStatement.setString(8, currentUsername);
            preparedStatement.setInt(9, yggdrasilId);

            preparedStatement.setInt(10, yggdrasilId);
            preparedStatement.setBytes(11, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setString(12, currentUsername);
            return preparedStatement.executeUpdate() != 0;
        }
    }
}
