package moe.caa.multilogin.core.database.handler;

import moe.caa.multilogin.core.database.SQLManager;
import moe.caa.multilogin.core.util.Pair;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SkinRestorerDataHandler {
    private static final String fieldSkinUrl = "skin_url";
    private static final String fieldRestorerValue = "skin_restorer_value";
    private static final String fieldRestorerSignature = "skin_restorer_signature";
    private final SQLManager sqlManager;

    /**
     * 构建这个皮肤修复数据管理类
     *
     * @param sqlManager 数据库管理类
     */
    public SkinRestorerDataHandler(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
    }

    public void init() throws SQLException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + SQLManager.getSkinRestorerTableName() + "(" +
                     fieldSkinUrl + " BINARY(32) NOT NULL, " +
                     fieldRestorerValue + " TEXT, " +
                     fieldRestorerSignature + " TEXT, " +
                     "PRIMARY KEY(" + fieldSkinUrl + "))"
             )) {
            preparedStatement.executeUpdate();
        }
    }

    /**
     * 写入一条新的皮肤修复数据
     */
    public void insertNew(String skinUrl, String texturesValue, String texturesSignature) throws SQLException, NoSuchAlgorithmException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("INSERT INTO %s (%s, %s, %s) VALUES(?, ?, ?)",
                             SQLManager.getSkinRestorerTableName(), fieldSkinUrl, fieldRestorerValue,
                             fieldRestorerSignature
                     ))) {

            preparedStatement.setBytes(1, MessageDigest.getInstance("SHA-256").digest(skinUrl.getBytes(StandardCharsets.UTF_8)));
            preparedStatement.setString(2, texturesValue);
            preparedStatement.setString(3, texturesSignature);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * 获取皮肤修复数据内容
     */
    public Pair<String, String> getSkinRestorerData(String skinUrl) throws SQLException, NoSuchAlgorithmException {
        try (Connection connection = sqlManager.getPool().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("SELECT %s, %s FROM %s WHERE %s = ? limit 1",
                             fieldRestorerValue, fieldRestorerSignature, SQLManager.getSkinRestorerTableName(), fieldSkinUrl
                     ))) {
            preparedStatement.setBytes(1, MessageDigest.getInstance("SHA-256").digest(skinUrl.getBytes(StandardCharsets.UTF_8)));
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Pair<>(resultSet.getString(1), resultSet.getString(2));
                }
                return null;
            }
        }
    }
}
