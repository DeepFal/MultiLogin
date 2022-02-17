package moe.caa.multilogin.core.database.handler;

import moe.caa.multilogin.core.database.SQLManager;
import moe.caa.multilogin.core.util.There;
import moe.caa.multilogin.core.util.ValueUtil;

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
     * 通过 onlineUuid 和 yggdrasilId 获取 inGameUuid 和 currentUsername 和 whitelist
     */
    public There<UUID, String, Boolean> getInGameUuidAndCurrentUsernameAndWhitelistByOnlineUuidAndYggdrasilId(UUID onlineUuid, int yggdrasilId) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("SELECT %s, %s, %s FROM %s WHERE %s = ? AND %s = ? LIMIT 1",
                             fieldInGameUuid, fieldCurrentUsername, fieldWhitelist,
                             SQLManager.getUserDataTableName(), fieldOnlineUuid, fieldYggdrasilId
                     ))) {
            preparedStatement.setBytes(1, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(2, yggdrasilId);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new There<>(
                            ValueUtil.bytesToUuid(resultSet.getBytes(1)),
                            resultSet.getString(2),
                            resultSet.getBoolean(3)
                    );
                }
                return null;
            }
        }
    }

    /**
     * 判断这个名字有没有被记录
     */
    public boolean hasUseByCurrentUsername(String currentUsername) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("SELECT 1 FROM %s WHERE %s = ? LIMIT 1",
                             SQLManager.getUserDataTableName(),
                             fieldCurrentUsername
                     ))) {
            preparedStatement.setString(1, currentUsername);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * 判断这个名字有没有被记录在与指定 inGameUuid 同行的地方
     */
    public boolean hasUseByCurrentUsernameAndInGameUuid(String currentUsername, UUID inGameUuid) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("SELECT 1 FROM %s WHERE %s = ? AND %s = ? LIMIT 1",
                             SQLManager.getUserDataTableName(),
                             fieldCurrentUsername, fieldInGameUuid
                     ))) {
            preparedStatement.setString(1, currentUsername);
            preparedStatement.setBytes(2, ValueUtil.uuidToBytes(inGameUuid));
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * 判断这个 UUID 有没有被使用
     */
    public boolean hasUseByInGameUuid(UUID inGameUuid) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("SELECT 1 FROM %s WHERE %s = ? LIMIT 1",
                             SQLManager.getUserDataTableName(),
                             fieldInGameUuid
                     ))) {
            preparedStatement.setBytes(1, ValueUtil.uuidToBytes(inGameUuid));

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * 获得下一个没有被使用过的 inGameUuid
     */
    public UUID getNextInGameUuid() throws SQLException {
        final UUID ret = UUID.randomUUID();
        return hasUseByInGameUuid(ret) ? getNextInGameUuid() : ret;
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
     * 更新玩家用户名和白名单
     */
    public void updateUsernameAndWhitelist(UUID onlineUuid, int yggdrasilId, String newUsername, boolean whitelist) throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("UPDATE %s SET %s = ?, %s = ? WHERE %s = ? AND %s = ? limit 1",
                             SQLManager.getUserDataTableName(), fieldCurrentUsername, fieldWhitelist,
                             fieldOnlineUuid, fieldYggdrasilId
                     ))) {
            preparedStatement.setString(1, newUsername);
            preparedStatement.setBoolean(2, whitelist);
            preparedStatement.setBytes(3, ValueUtil.uuidToBytes(onlineUuid));
            preparedStatement.setInt(4, yggdrasilId);
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
}
