package net.paguo.statistics.snmp.repositories.impl;

import net.paguo.statistics.snmp.database.WriteProxy;
import net.paguo.statistics.snmp.model.HostResult;
import net.paguo.statistics.snmp.repositories.UptimeRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;

public class UptimeRepositoryImpl implements UptimeRepository {
    private final WriteProxy writeProxy;
    private static final String SAVE_UPTIME_SQL = "INSERT INTO UPTIME(dt, uptime, cisco) VALUES(?, ?, ?)";


    public UptimeRepositoryImpl(WriteProxy writeProxy) {
        this.writeProxy = writeProxy;
    }

    @Override
    public int saveUptime(Collection<HostResult> records, final Timestamp now) throws SQLException {
        return writeProxy.run(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_UPTIME_SQL)) {
                for (HostResult record: records) {
                    String router = record.getHostAddress();
                    long uptime = record.getUptime();
                    preparedStatement.setTimestamp(1, now);
                    preparedStatement.setLong(2, uptime);
                    preparedStatement.setString(3, router);
                    preparedStatement.addBatch();
                }
                int[] ints = preparedStatement.executeBatch();
                return ints.length;
            }
        });
    }
}
