package moe.caa.multilogin.core.database.handler;

import moe.caa.multilogin.core.Pair;
import moe.caa.multilogin.core.ValueUtil;
import moe.caa.multilogin.core.database.SQLManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserDataHandler {
    private static final String fieldOnlineUuid = "online_uuid";
    private static final String fieldYggdrasilId = "yggdrasil_id";
    private static final String fieldInGameUuid = "in_game_uuid";
    private static final String fieldCurrentUsername = "current_username";
    private static final String fieldWhitelist = "whitelist";
    private final SQLManager sqlManager;

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
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + SQLManager.getUserDataTableName() + "(" +
                     fieldOnlineUuid + " BINARY(16) NOT NULL, " +
                     fieldYggdrasilId + " TINYINT, " +
                     fieldInGameUuid + " BINARY(16) NOT NULL, " +
                     fieldCurrentUsername + " VARCHAR(32) NOT NULL, " +
                     fieldWhitelist + " BOOL, " +
                     "PRIMARY KEY(" + fieldOnlineUuid + ", " + fieldYggdrasilId + "))"
             )) {
            preparedStatement.executeUpdate();
        }
    }

    /**
     * 写入一条新的玩家数据
     */
    public void insertNew(UUID onlineUuid, int yggdrasilId, UUID inGameUuid, String currentUsername, boolean whitelist) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES(?, ?, ?, ?, ?)",
                             SQLManager.getUserDataTableName(), fieldOnlineUuid, fieldYggdrasilId,
                             fieldInGameUuid, fieldCurrentUsername, fieldWhitelist
                     ))) {
            preparedStatement.setBytes(1, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(2, yggdrasilId);
            preparedStatement.setBytes(3, ValueUtil.uuidToBytes(inGameUuid));
            preparedStatement.setString(4, currentUsername);
            preparedStatement.setBoolean(5, whitelist);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * 更新玩家用户名
     */
    public void updateUsername(UUID onlineUuid, int yggdrasilId, String newUsername) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ? limit 1",
                             SQLManager.getUserDataTableName(), fieldCurrentUsername,
                             fieldOnlineUuid, fieldYggdrasilId
                     ))) {
            preparedStatement.setString(1, newUsername);
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(3, yggdrasilId);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * 更新玩家游戏内UUID
     */
    public void updateInGameUuid(UUID onlineUuid, int yggdrasilId, UUID newInGameUuid) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ? limit 1",
                             SQLManager.getUserDataTableName(), fieldInGameUuid,
                             fieldOnlineUuid, fieldYggdrasilId
                     ))) {
            preparedStatement.setBytes(1, ValueUtil.uuidToBytes(newInGameUuid));
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(3, yggdrasilId);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * 更新玩家白名单数据
     */
    public void updateWhitelist(UUID onlineUuid, int yggdrasilId, boolean newWhitelist) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ? limit 1",
                             SQLManager.getUserDataTableName(), fieldWhitelist,
                             fieldOnlineUuid, fieldYggdrasilId
                     ))) {
            preparedStatement.setBoolean(1, newWhitelist);
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(3, yggdrasilId);
            preparedStatement.executeUpdate();
        }
    }

    public Set<Integer> getYggdrasilIdByCurrentUsername(String currentUsername) throws SQLException {
        try (Connection conn = sqlManager.getPool().getConnection();
             PreparedStatement ps = conn.prepareStatement(String.format("SELECT %s FROM %s WHERE %s = ?",
                     fieldYggdrasilId, SQLManager.getUserDataTableName(),
                     fieldCurrentUsername
             ))) {
            ps.setString(1, currentUsername);
            try (ResultSet resultSet = ps.executeQuery()) {
                Set<Integer> ret = new HashSet<>();
                while (resultSet.next()) {
                    ret.add(resultSet.getInt(1));
                }
                return ret;
            }
        }
    }

    /**
     * 获得玩家游戏内UUID
     */
    public UUID getInGameUuid(UUID onlineUuid, int yggdrasilId) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ? limit 1",
                             fieldInGameUuid, SQLManager.getUserDataTableName(),
                             fieldOnlineUuid, fieldYggdrasilId
                     ))) {
            preparedStatement.setBytes(1, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(2, yggdrasilId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return ValueUtil.bytesToUuid(resultSet.getBytes(1));
                }
                return null;
            }
        }
    }

    /**
     * 获得玩家白名单数据
     */
    public boolean getWhitelist(UUID onlineUuid, int yggdrasilId) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ? limit 1",
                             fieldWhitelist, SQLManager.getUserDataTableName(),
                             fieldOnlineUuid, fieldYggdrasilId
                     ))) {
            preparedStatement.setBytes(1, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(2, yggdrasilId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
                return false;
            }
        }
    }

    /**
     * 获得玩家白名单数据
     */
    public Set<Pair<UUID, Integer>> getAllWhitelist(boolean whitelist) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("SELECT %s, %s FROM %s WHERE %s = ?",
                             fieldOnlineUuid, fieldYggdrasilId,
                             SQLManager.getUserDataTableName(), fieldWhitelist
                     ))) {

            preparedStatement.setBoolean(1, whitelist);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                Set<Pair<UUID, Integer>> ret = new HashSet<>();
                if (resultSet.next()) {
                    ret.add(new Pair<>(ValueUtil.bytesToUuid(resultSet.getBytes(1)), resultSet.getInt(2)));
                }
                return ret;
            }
        }
    }
}
