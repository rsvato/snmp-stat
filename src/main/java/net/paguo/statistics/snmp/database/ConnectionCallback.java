package net.paguo.statistics.snmp.database;

import java.sql.Connection;

public interface ConnectionCallback<T> {
    T execute(Connection connection);
}
