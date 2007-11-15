package net.paguo.statistics.snmp.database;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import net.paguo.statistics.snmp.model.HostDefinition;

/**
 * User: slava
 * Date: 26.04.2007
 * Time: 0:04:16
 * Version: $Id$
 */
public class DBProxy {
    private DataSource ds;

    DBProxy() {
        ds = new PGSimpleDataSource();
        String dbhost = System.getProperty("dbhost");
        String database = System.getProperty("database");
        String username = "root";
        String password = "test12~";
        ((PGSimpleDataSource) ds).setUser(username);
        ((PGSimpleDataSource) ds).setPassword(password);
        ((PGSimpleDataSource) ds).setServerName(dbhost);
        ((PGSimpleDataSource) ds).setDatabaseName(database);
    }

    DBProxy(Properties props) {
        ds = new PGSimpleDataSource();
        String dbhost = props.getProperty(DBProxyFactory.HOST_KEY);
        String database = props.getProperty(DBProxyFactory.DATABASE_KEY);
        String username = props.getProperty(DBProxyFactory.USER_KEY);
        String password = props.getProperty(DBProxyFactory.PASSWORD_KEY);
        ((PGSimpleDataSource) ds).setUser(username);
        ((PGSimpleDataSource) ds).setPassword(password);
        ((PGSimpleDataSource) ds).setServerName(dbhost);
        ((PGSimpleDataSource) ds).setDatabaseName(database);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void closeConnection(Connection c) throws SQLException {
        c.close();
    }

    public void saveUptime(HostDefinition definition, long l) {
    }
}
