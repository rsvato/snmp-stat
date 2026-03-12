package net.paguo.statistics.snmp.repositories;

import net.paguo.statistics.snmp.model.HostResult;

import java.sql.SQLException;
import java.util.Collection;

public interface RoutersRepository {
    int saveInterfaces(Collection<HostResult> results) throws SQLException;
}
