package net.paguo.statistics.snmp.repositories.impl;

import net.paguo.statistics.snmp.database.ReadProxy;
import net.paguo.statistics.snmp.repositories.TrafficRecordsReadRepository;

import java.sql.*;

public class TrafficRecordsReadRepositoryImpl implements TrafficRecordsReadRepository {
    private final ReadProxy dbProxy;

    private static final String CHECK_TRAFFIC_SQL = "select count(*) from tr where cisco = ? and interface =? and dt = ?";

    public TrafficRecordsReadRepositoryImpl(ReadProxy dbProxy) {
        this.dbProxy = dbProxy;
    }

    @Override
    public boolean checkTrafficRecordExists(String hostAddress, String iface, Timestamp time) throws SQLException {
        return dbProxy.withConnection(connection -> {
            boolean result;
            try(PreparedStatement preparedStatement = connection.prepareStatement(CHECK_TRAFFIC_SQL)) {
                preparedStatement.setString(1, hostAddress);
                preparedStatement.setString(2, iface);
                preparedStatement.setTimestamp(3, time);
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    result = rs.next() && rs.getInt(1) > 0;
                }
            }
            return result;
        });
    }
}
