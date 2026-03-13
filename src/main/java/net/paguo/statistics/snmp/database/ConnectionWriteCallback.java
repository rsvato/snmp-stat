package net.paguo.statistics.snmp.database;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionWriteCallback {
    int run(Connection connection) throws SQLException;
}
