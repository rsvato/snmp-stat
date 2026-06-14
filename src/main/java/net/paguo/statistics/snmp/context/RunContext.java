package net.paguo.statistics.snmp.context;

import net.paguo.statistics.snmp.commands.SnmpRunner;
import net.paguo.statistics.snmp.database.DBProxy;
import net.paguo.statistics.snmp.database.DBProxyFactory;
import net.paguo.statistics.snmp.dumper.Dumper;
import net.paguo.statistics.snmp.repositories.*;
import net.paguo.statistics.snmp.repositories.impl.*;

public class RunContext {

    private final DBProxy dbProxy;

    public RunContext(DBProxy dbProxy) {
        this.dbProxy = dbProxy;
    }

    public HostRepository getHostRepository() {
        return new HostRepositoryImpl(dbProxy);
    }

    public TrafficRecordsWriteRepository getTrafficRecordsWriteRepository() {
        return new TrafficRecordsWriteRepositoryImpl(dbProxy);
    }

    public RoutersRepository getRoutersRepository() {
        return new RoutersRepositoryImpl(dbProxy);
    }

    public UptimeRepository getUptimeRepository() {
        return new UptimeRepositoryImpl(dbProxy);
    }

    public CollectionAuditRepository getColllectionAuditRepository() {
        return new CollectionAuditRepositoryImpl(dbProxy);
    }

    public HostQuery getHostQuery() {
        return new HostQuery(
                getHostRepository(),
                getTrafficRecordsWriteRepository(),
                getRoutersRepository(),
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

    public static RunContext defaultContext() {
        return new RunContext(DBProxyFactory.getDBProxy());
    }
}
