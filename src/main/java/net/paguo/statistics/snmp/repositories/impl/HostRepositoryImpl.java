package net.paguo.statistics.snmp.repositories.impl;

import net.paguo.statistics.snmp.database.ReadProxy;
import net.paguo.statistics.snmp.model.HostDefinition;
import net.paguo.statistics.snmp.repositories.HostRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class HostRepositoryImpl implements HostRepository {
    private final ReadProxy dbProxy;
    private static final String GET_HOSTS_SQL ="select address, community from snmp_addresses where is_active";

    public HostRepositoryImpl(ReadProxy dbProxy) {
        this.dbProxy = dbProxy;
    }

    @Override
    public Set<HostDefinition> allActive() throws SQLException {
        return dbProxy.withConnection(conn -> {
            Set<HostDefinition> result = new HashSet<>();
            try (PreparedStatement ps = conn.prepareStatement(GET_HOSTS_SQL)) {
                try(ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        HostDefinition hostDefinition = new HostDefinition(rs.getString("address"),
                                rs.getString("community"));
                        result.add(hostDefinition);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return result;
        });
    }

}
