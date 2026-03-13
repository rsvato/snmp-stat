package net.paguo.statistics.snmp.repositories.impl;

import net.paguo.statistics.snmp.database.WriteProxy;
import net.paguo.statistics.snmp.model.HostResult;
import net.paguo.statistics.snmp.repositories.RoutersRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

public class RoutersRepositoryImpl implements RoutersRepository {
    private final WriteProxy dbProxy;
    private static final String ADD_INTERFACE = "INSERT INTO cisco_iface (cisco, interface) VALUES (?, ?) ON CONFLICT(cisco, interface) DO NOTHING";

    public RoutersRepositoryImpl(WriteProxy dbProxy) {
        this.dbProxy = dbProxy;
    }

    @Override
    public int saveInterfaces(Collection<HostResult> results) throws SQLException {
        return dbProxy.run(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(ADD_INTERFACE)) {
                for (HostResult result : results) {
                    String hostAddress = result.getHostAddress();
                    for (String iface : result.getInterfaces().values()) {
                        preparedStatement.setString(1, hostAddress);
                        preparedStatement.setString(2, iface);
                        preparedStatement.addBatch();
                    }
                }
                int[] ints = preparedStatement.executeBatch();
                return ints != null ? Arrays.stream(ints).filter(i -> i > 0).sum() : 0;
            }
        });
    }
}
