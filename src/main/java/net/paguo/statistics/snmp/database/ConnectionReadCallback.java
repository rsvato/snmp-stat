package net.paguo.statistics.snmp.database;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionReadCallback<T> {
    T execute(Connection connection) throws SQLException;
}
