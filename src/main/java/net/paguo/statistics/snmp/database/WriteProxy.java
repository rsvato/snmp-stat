package net.paguo.statistics.snmp.database;

import java.sql.SQLException;

public interface WriteProxy {
    int run(ConnectionWriteCallback executor) throws SQLException;
}
