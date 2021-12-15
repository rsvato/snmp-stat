package net.paguo.statistics.snmp.database;

import net.paguo.statistics.snmp.model.HostDefinition;
import org.postgresql.ds.PGSimpleDataSource;

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
public class DBProxy {
    private final DataSource ds;

    DBProxy(Properties props) {
        ds = new PGSimpleDataSource();
        String dbhost = props.getProperty(DBProxyFactory.HOST_KEY);
        String database = props.getProperty(DBProxyFactory.DATABASE_KEY);
        String username = props.getProperty(DBProxyFactory.USER_KEY);
        String password = props.getProperty(DBProxyFactory.PASSWORD_KEY);
        ((PGSimpleDataSource) ds).setUser(username);
        ((PGSimpleDataSource) ds).setPassword(password);
        ((PGSimpleDataSource) ds).setServerNames(new String[] {dbhost});
        ((PGSimpleDataSource) ds).setDatabaseName(database);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void closeConnection(Connection c) throws SQLException {
        c.close();
    }

    public void saveUptime(HostDefinition definition, long l) {
    }
}
