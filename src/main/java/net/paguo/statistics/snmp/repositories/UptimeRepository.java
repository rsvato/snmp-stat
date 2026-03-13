package net.paguo.statistics.snmp.repositories;

import net.paguo.statistics.snmp.model.HostResult;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;

public interface UptimeRepository {
    int saveUptime(Collection<HostResult> results, Timestamp now) throws SQLException;
}
