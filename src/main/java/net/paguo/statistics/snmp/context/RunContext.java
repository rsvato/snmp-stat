package net.paguo.statistics.snmp.context;

import net.paguo.statistics.snmp.commands.SnmpRunner;
import net.paguo.statistics.snmp.database.DBProxy;
import net.paguo.statistics.snmp.database.DBProxyFactory;
import net.paguo.statistics.snmp.dumper.Dumper;
import net.paguo.statistics.snmp.repositories.*;
import net.paguo.statistics.snmp.repositories.impl.*;

public class RunContext {

    public DBProxy getDbProxy() {
        return DBProxyFactory.getDBProxy();
    }

    public HostRepository getHostRepository() {
        return new HostRepositoryImpl(getDbProxy());
    }

    public TrafficRecordsReadRepository getTrafficReadRepository() {
        return new TrafficRecordsReadRepositoryImpl(getDbProxy());
    }

    public TrafficRecordsWriteRepository getTrafficRecordsWriteRepository() {
        return new TrafficRecordsWriteRepositoryImpl(getDbProxy());
    }

    public RoutersRepository getRputersRepository() {
        return new RoutersRepositoryImpl(getDbProxy());
    }

    public UptimeRepository getUptimeRepository() {
        return new UptimeRepositoryImpl(getDbProxy());
    }

    public CollectionAuditRepository getColllectionAuditRepository() {
        return new CollectionAuditRepositoryImpl(getDbProxy());
    }

    public HostQuery getHostQuery() {
        return new HostQuery(
                getHostRepository(),
                getTrafficRecordsWriteRepository(),
                getRputersRepository(),
                getUptimeRepository(),
                getColllectionAuditRepository()
        );
    }

    public Dumper getDumper() {
        return new Dumper();
    }

    public SnmpRunner getSnmpRunner() {
        return new SnmpRunner();
    }

}
