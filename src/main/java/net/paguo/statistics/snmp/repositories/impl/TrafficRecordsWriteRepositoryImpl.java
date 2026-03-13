package net.paguo.statistics.snmp.repositories.impl;

import net.paguo.statistics.snmp.database.WriteProxy;
import net.paguo.statistics.snmp.model.HostResult;
import net.paguo.statistics.snmp.repositories.TrafficRecordsWriteRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class TrafficRecordsWriteRepositoryImpl implements TrafficRecordsWriteRepository {
    private final WriteProxy writeProxy;
    private static final String INSERT_TRAFFIC_SQL = "INSERT INTO tr(cisco, interface, inoctets, outoctets, dt) " +
            "VALUES (?, ?, ?, ?, ?) ON CONFLICT (dt, cisco, interface) DO NOTHING";

    public TrafficRecordsWriteRepositoryImpl(WriteProxy dbProxy) {
        this.writeProxy = dbProxy;
    }

    @Override
    public int saveTraffic(Collection<HostResult> records, final Timestamp now) throws SQLException {
        return writeProxy.run(connection -> {
            try(PreparedStatement preparedStatement = connection.prepareStatement(INSERT_TRAFFIC_SQL)) {
                for (HostResult record: records) {
                    String router = record.getHostAddress();
                    for (Map.Entry<Long,String> entry: record.getInterfaces().entrySet()) {
                        String networkInterface = entry.getValue();
                        long incomingBytes = Long.parseLong(record.getInputs().get(entry.getKey()));
                        long outcomingBytes = Long.parseLong(record.getOutputs().get(entry.getKey()));
                        preparedStatement.setString(1, router);
                        preparedStatement.setString(2, networkInterface);
                        preparedStatement.setLong(3, incomingBytes);
                        preparedStatement.setLong(4, outcomingBytes);
                        preparedStatement.setTimestamp(5, now);
                        preparedStatement.addBatch();
                    }
                }
                int[] results = preparedStatement.executeBatch();
                return results != null ? Arrays.stream(results).filter(i -> i > 0).sum() : 0;
            }
        });
    }
}
