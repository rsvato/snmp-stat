package net.paguo.statistics.snmp.repositories;

import java.sql.SQLException;
import java.sql.Timestamp;

public interface TrafficRecordsReadRepository {
    boolean checkTrafficRecordExists(String hostAddress, String iface, Timestamp time) throws SQLException;
}
