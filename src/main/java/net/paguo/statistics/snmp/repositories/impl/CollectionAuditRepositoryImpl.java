package net.paguo.statistics.snmp.repositories.impl;

import net.paguo.statistics.snmp.database.WriteProxy;
import net.paguo.statistics.snmp.model.HostResult;
import net.paguo.statistics.snmp.repositories.CollectionAuditRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;

public class CollectionAuditRepositoryImpl implements CollectionAuditRepository {
    private final WriteProxy writeProxy;
    private static final String AUDIT_LOG_SQL = "insert into last_snmp_checks (cisco, last_check) values (?, ?)";

    public CollectionAuditRepositoryImpl(WriteProxy writeProxy) {
        this.writeProxy = writeProxy;
    }

    @Override
    public int writeAuditLog(Collection<HostResult> records, final Timestamp now) throws SQLException {
        return writeProxy.run(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(AUDIT_LOG_SQL)) {
                for (HostResult record : records) {
                    preparedStatement.setString(1, record.getHostAddress());
                    preparedStatement.setTimestamp(2, now);
                    preparedStatement.addBatch();
                }
                int[] ints = preparedStatement.executeBatch();
                return ints.length;
            }
        });
    }
}
