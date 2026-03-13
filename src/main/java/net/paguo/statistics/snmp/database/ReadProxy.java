package net.paguo.statistics.snmp.database;

import java.sql.SQLException;

public interface ReadProxy {
    <T> T withConnection(ConnectionReadCallback<T> executor) throws SQLException;
}
