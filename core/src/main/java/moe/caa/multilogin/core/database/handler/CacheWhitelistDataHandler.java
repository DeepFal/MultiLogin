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
    public boolean insertNewByUsername(String currentUsername) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     MessageFormat.format("INSERT INTO {0} ({1}) SELECT ? FROM DUAL WHERE NOT EXISTS(SELECT {1} FROM {0} WHERE {1} = ?)",
                             SQLManager.getCacheWhitelistTableName(), fieldCurrentUsername
                     ))) {
            preparedStatement.setString(1, currentUsername);
            preparedStatement.setString(2, currentUsername);
            return preparedStatement.executeUpdate() != 0;
        }
    }

    /**
     * 插入一条记录
     */
    public boolean insertNewByOnlineUuid(UUID onlineUuid) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     MessageFormat.format("INSERT INTO {0} ({1}) SELECT ? FROM DUAL WHERE NOT EXISTS(SELECT {1} FROM {0} WHERE {1} = ?)",
                             SQLManager.getCacheWhitelistTableName(), fieldOnlineUuid
                     ))) {
            preparedStatement.setBytes(1, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(onlineUuid));
            return preparedStatement.executeUpdate() != 0;
        }
    }

    /**
     * 插入一条记录
     */
    public boolean insertNewByUsernameAndOnlineUuid(String currentUsername, UUID onlineUuid) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     MessageFormat.format("INSERT INTO {0} ({1}, {2}) SELECT ?, ? FROM DUAL WHERE NOT EXISTS(SELECT {1}, {2} FROM {0} WHERE {1} = ? AND {2} = ?)",
                             SQLManager.getCacheWhitelistTableName(), fieldCurrentUsername,
                             fieldOnlineUuid
                     ))) {
            preparedStatement.setString(1, currentUsername);
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setString(3, currentUsername);
            preparedStatement.setBytes(4, ValueUtil.uuidToBytes(onlineUuid));
            return preparedStatement.executeUpdate() != 0;
        }
    }

    /**
     * 插入一条记录
     */
    public boolean insertNewByUsernameAndYggdrasilId(String currentUsername, int yggdrasilId) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     MessageFormat.format("INSERT INTO {0} ({1}, {2}) SELECT ?, ? FROM DUAL WHERE NOT EXISTS(SELECT {1}, {2} FROM {0} WHERE {1} = ? AND {2} = ?)",
                             SQLManager.getCacheWhitelistTableName(), fieldCurrentUsername,
                             fieldYggdrasilId
                     ))) {
            preparedStatement.setString(1, currentUsername);
            preparedStatement.setInt(2, yggdrasilId);
            preparedStatement.setString(3, currentUsername);
            preparedStatement.setInt(4, yggdrasilId);
            return preparedStatement.executeUpdate() != 0;
        }
    }

    /**
     * 插入一条记录
     */
    public boolean insertNewByOnlineUuidAndYggdrasilId(UUID onlineUuid, int yggdrasilId) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     MessageFormat.format("INSERT INTO {0} ({1}, {2}) SELECT ?, ? FROM DUAL WHERE NOT EXISTS(SELECT {1}, {2} FROM {0} WHERE {1} = ? AND {2} = ?)",
                             SQLManager.getCacheWhitelistTableName(), fieldOnlineUuid,
                             fieldYggdrasilId
                     ))) {
            preparedStatement.setBytes(1, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(2, yggdrasilId);
            preparedStatement.setBytes(3, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(4, yggdrasilId);
            return preparedStatement.executeUpdate() != 0;
        }
    }

    /**
     * 插入一条记录
     */
    public boolean insertNewByUsernameOnlineUuidAndYggdrasilId(String currentUsername, UUID onlineUuid, int yggdrasilId) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     MessageFormat.format("INSERT INTO {0} ({1}, {2}, {3}) SELECT ?, ?, ? FROM DUAL WHERE NOT EXISTS(SELECT {1}, {2}, {3} FROM {0} WHERE {1} = ? AND {2} = ? AND {3} = ?)",
                             SQLManager.getCacheWhitelistTableName(), fieldCurrentUsername,
                             fieldOnlineUuid, fieldYggdrasilId
                     ))) {
            preparedStatement.setString(1, currentUsername);
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(3, yggdrasilId);
            preparedStatement.setString(4, currentUsername);
            preparedStatement.setBytes(5, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(6, yggdrasilId);
            return preparedStatement.executeUpdate() != 0;
        }
    }

    /**
     * 移除一条数据
     */
    public int removeByCurrentUsername(String currentUsername) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("DELETE FROM %s WHERE %s = ?",
                             SQLManager.getCacheWhitelistTableName(), fieldCurrentUsername
                     ))) {
            preparedStatement.setString(1, currentUsername);
            return preparedStatement.executeUpdate();
        }
    }

    /**
     * 移除一条数据
     */
    public int removeByYggdrasilId(int yggdrasilId) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("DELETE FROM %s WHERE %s = ?",
                             SQLManager.getCacheWhitelistTableName(), fieldYggdrasilId
                     ))) {
            preparedStatement.setInt(1, yggdrasilId);
            return preparedStatement.executeUpdate();
        }
    }

    /**
     * 移除一条数据
     */
    public int removeByOnlineUuid(UUID onlineUuid) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("DELETE FROM %s WHERE %s = ?",
                             SQLManager.getCacheWhitelistTableName(), fieldOnlineUuid
                     ))) {
            preparedStatement.setBytes(1, ValueUtil.uuidToBytes(onlineUuid));
            return preparedStatement.executeUpdate();
        }
    }

    /**
     * 移除一条数据
     */
    public int removeByCurrentUsernameAndOnlineUuid(String currentUsername, UUID onlineUuid) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("DELETE FROM %s WHERE %s = ?, %s = ?",
                             SQLManager.getCacheWhitelistTableName(), fieldCurrentUsername, fieldOnlineUuid
                     ))) {
            preparedStatement.setString(1, currentUsername);
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(onlineUuid));
            return preparedStatement.executeUpdate();
        }
    }

    /**
     * 移除一条数据
     */
    public int removeByCurrentUsernameAndYggdrasilId(String currentUsername, int yggdrasilId) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("DELETE FROM %s WHERE %s = ?, %s = ?",
                             SQLManager.getCacheWhitelistTableName(), fieldCurrentUsername, fieldYggdrasilId
                     ))) {
            preparedStatement.setString(1, currentUsername);
            preparedStatement.setInt(2, yggdrasilId);
            return preparedStatement.executeUpdate();
        }
    }

    /**
     * 移除一条数据
     */
    public int removeByOnlineUuidAndYggdrasilId(UUID onlineUuid, int yggdrasilId) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("DELETE FROM %s WHERE %s = ?, %s = ?",
                             SQLManager.getCacheWhitelistTableName(), fieldOnlineUuid, fieldYggdrasilId
                     ))) {
            preparedStatement.setBytes(1, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(2, yggdrasilId);
            return preparedStatement.executeUpdate();
        }
    }

    /**
     * 移除一条数据
     */
    public int removeByOnlineUuidAndYggdrasilId(String currentUsername, UUID onlineUuid, int yggdrasilId) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("DELETE FROM %s WHERE %s = ?, %s = ?, %s = ?",
                             SQLManager.getCacheWhitelistTableName(), fieldCurrentUsername, fieldOnlineUuid, fieldYggdrasilId
                     ))) {
            preparedStatement.setString(1, currentUsername);
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(3, yggdrasilId);
            return preparedStatement.executeUpdate();
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

    /**
     * 移除全部的缓存白名单
     */
    public int removeAllCacheWhitelist() throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " +
                     SQLManager.getCacheWhitelistTableName()
             )) {
            return preparedStatement.executeUpdate();
        }
    }
}
