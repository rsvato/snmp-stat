package net.paguo.statistics.snmp.context;

import net.paguo.statistics.snmp.commands.SnmpRunner;
import net.paguo.statistics.snmp.database.DBProxy;
import net.paguo.statistics.snmp.database.DBProxyFactory;
import net.paguo.statistics.snmp.dumper.Dumper;
import net.paguo.statistics.snmp.repositories.HostQuery;
import net.paguo.statistics.snmp.repositories.HostRepository;
import net.paguo.statistics.snmp.repositories.RoutersRepository;
import net.paguo.statistics.snmp.repositories.TrafficRecordsReadRepository;
import net.paguo.statistics.snmp.repositories.impl.HostRepositoryImpl;
import net.paguo.statistics.snmp.repositories.impl.RoutersRepositoryImpl;
import net.paguo.statistics.snmp.repositories.impl.TrafficRecordsReadRepositoryImpl;

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

    public RoutersRepository getRputersRepository() {
        return new RoutersRepositoryImpl(getDbProxy());
    }

    public HostQuery getHostQuery() {
        return new HostQuery(getHostRepository(), getTrafficReadRepository(),
                getRputersRepository());
    }

    public Dumper getDumper() {
        return new Dumper();
    }

    public SnmpRunner getSnmpRunner() {
        return new SnmpRunner();
    }

}
