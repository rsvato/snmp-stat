package net.paguo.statistics.snmp.context;

import net.paguo.statistics.snmp.commands.SnmpRunner;
import net.paguo.statistics.snmp.database.DBProxy;
import net.paguo.statistics.snmp.database.DBProxyFactory;
import net.paguo.statistics.snmp.dumper.Dumper;
import net.paguo.statistics.snmp.repositories.*;
import net.paguo.statistics.snmp.repositories.impl.*;

public class RunContext {

    private final HostRepository hostRepository;
    private final TrafficRecordsWriteRepository trafficRecordsWriteRepository;
    private final RoutersRepository routersRepository;
    private final UptimeRepository uptimeRepository;
    private final CollectionAuditRepository collectionAuditRepository;
    private final HostQuery hostQuery;
    private final SnmpRunner snmpRunner;

    public RunContext(DBProxy dbProxy) {
        this.hostRepository = new HostRepositoryImpl(dbProxy);
        this.trafficRecordsWriteRepository = new TrafficRecordsWriteRepositoryImpl(dbProxy);
        this.routersRepository = new RoutersRepositoryImpl(dbProxy);
        this.uptimeRepository = new UptimeRepositoryImpl(dbProxy);
        this.collectionAuditRepository = new CollectionAuditRepositoryImpl(dbProxy);
        this.hostQuery = new HostQuery(
                hostRepository,
                trafficRecordsWriteRepository,
                routersRepository,
                uptimeRepository,
                collectionAuditRepository
        );
        this.snmpRunner = new SnmpRunner();
    }

    public HostRepository getHostRepository() {
        return hostRepository;
    }

    public TrafficRecordsWriteRepository getTrafficRecordsWriteRepository() {
        return trafficRecordsWriteRepository;
    }

    public RoutersRepository getRoutersRepository() {
        return routersRepository;
    }

    public UptimeRepository getUptimeRepository() {
        return uptimeRepository;
    }

    public CollectionAuditRepository getColllectionAuditRepository() {
        return collectionAuditRepository;
    }

    public HostQuery getHostQuery() {
        return hostQuery;
    }

    public Dumper getDumper() {
        String dumpDir = System.getProperty("dump.dir");
        return new Dumper(dumpDir);
    }

    public SnmpRunner getSnmpRunner() {
        return snmpRunner;
    }

    public static RunContext defaultContext() {
        return new RunContext(DBProxyFactory.getDBProxy());
    }
}
