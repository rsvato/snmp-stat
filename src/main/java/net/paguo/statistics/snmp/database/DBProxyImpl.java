package net.paguo.statistics.snmp.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * User: slava
 * Date: 26.04.2007
 * Time: 0:04:16
 * Version: $Id$
 */
public class DBProxyImpl implements DBProxy {
    private final HikariDataSource ds;

    DBProxyImpl(Properties props) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty(DBProxyFactory.DB_URL_KEY));
        config.setUsername(props.getProperty(DBProxyFactory.USER_KEY));
        config.setPassword(props.getProperty(DBProxyFactory.PASSWORD_KEY));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setPoolName("snmp-stat-pool");
        this.ds = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    @Override
    public <T> T withConnection(ConnectionReadCallback<T> executor) throws SQLException {
        try (Connection conn = getConnection()) {
            return executor.execute(conn);
        }
    }

    @Override
    public int run(ConnectionWriteCallback executor) throws SQLException {
        try (Connection conn = getConnection()) {
            return executor.run(conn);
        }
    }

    /** Package-private for testing — provides access to the underlying DataSource. */
    DataSource getDataSource() {
        return ds;
    }
}
