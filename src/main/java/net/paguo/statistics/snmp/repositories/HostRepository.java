package net.paguo.statistics.snmp.repositories;

import net.paguo.statistics.snmp.model.HostDefinition;

import java.sql.SQLException;
import java.util.Set;

public interface HostRepository {
    Set<HostDefinition> allActive() throws SQLException;
}
