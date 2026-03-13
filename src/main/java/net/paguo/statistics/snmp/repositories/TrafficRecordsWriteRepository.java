package net.paguo.statistics.snmp.repositories;

import net.paguo.statistics.snmp.model.HostResult;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;

public interface TrafficRecordsWriteRepository {
    int saveTraffic(Collection<HostResult> results, Timestamp now) throws SQLException;
}
