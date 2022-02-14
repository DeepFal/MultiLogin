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
                     fieldYggdrasilId + " TINYINT DEFAULT -1 )"
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
                                     "({1} = -1 " +
                                     "and ((" +
                                     "{2} is null " +
                                     "and {3} = ?" + // 只判断 currentUsername
                                     ") or (" +
                                     "{2} is not null " +
                                     "and ((" +
                                     "{3} is null " +
                                     "and {2} = ?" + // 只判断 onlineUuid
                                     ") or (" +
                                     "{3} is not null " +
                                     "and {2} = ? and {3} = ? " + // 判断 currentUsername 和 onlineUuid
                                     ")))) " +
                                     "or ({1} <> -1 " +
                                     "and (({2} is null " +
                                     "and {3} = ? and {1} = ?" + // 判断 currentUsername 和 yggdrasilId
                                     ") or (" +
                                     "{2} is not null " +
                                     "and ((" +
                                     "{3} is null and {2} = ? and {1} = ?" + // 判断 onlineUuid 和 yggdrasilId
                                     ") or (" +
                                     "{3} is not null " +
                                     "and {2} = ? and {3} = ? and {1} = ?" + // 判断 onlineUuid 和 yggdrasilId 和 currentUsername
                                     ")))))"
                             , SQLManager.getCacheWhitelistTableName(),
                             fieldYggdrasilId, fieldOnlineUuid, currentUsername
                     ))) {

            preparedStatement.setString(1, currentUsername);
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setBytes(3, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setString(4, currentUsername);
            preparedStatement.setString(5, currentUsername);
            preparedStatement.setInt(6, yggdrasilId);
            preparedStatement.setBytes(7, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(8, yggdrasilId);
            preparedStatement.setBytes(9, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setString(10, currentUsername);
            preparedStatement.setInt(11, yggdrasilId);
            return preparedStatement.executeUpdate() != 0;
        }
    }
}
