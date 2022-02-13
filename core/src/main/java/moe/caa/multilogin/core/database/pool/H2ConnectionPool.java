package moe.caa.multilogin.core.database.pool;

import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * H2 数据库链接池
 */
public class H2ConnectionPool implements ISQLConnectionPool {
    private static final String URL = "jdbc:h2:{0};TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0";
    private final JdbcConnectionPool cp;

    /**
     * 构建数据库链接池
     */
    public H2ConnectionPool(String filePath, String user, String password) {
        String url = URL.replace("{0}", filePath);
        cp = JdbcConnectionPool.create(url, user, password);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return cp.getConnection();
    }

    @Override
    public String name() {
        return "H2";
    }

    @Override
    public void close() {
        cp.dispose();
    }
}
