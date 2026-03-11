package net.paguo.statistics.snmp.repositories.impl;

import net.paguo.statistics.snmp.database.DBProxy;
import net.paguo.statistics.snmp.model.HostDefinition;
import net.paguo.statistics.snmp.repositories.HostRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class HostRepositoryImpl implements HostRepository {
    private final DBProxy dbProxy;
    private final String query ="select address, community from snmp_addresses where is_active";

    public HostRepositoryImpl(DBProxy dbProxy) {
        this.dbProxy = dbProxy;
    }
    @Override
    public Set<HostDefinition> allActive() throws SQLException {
        return dbProxy.withConnection(conn -> {
            Set<HostDefinition> result = new HashSet<>();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                try(ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        HostDefinition hostDefinition = buildHostDefinition(rs);
                        result.add(hostDefinition);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return result;
        });
    }

    private static HostDefinition buildHostDefinition(ResultSet rs) throws SQLException {
        HostDefinition hostDefinition = new HostDefinition();
        hostDefinition.setHostAddress(rs.getString("address"));
        hostDefinition.setCommunity(rs.getString("community"));
        return hostDefinition;
    }
}
