package net.paguo.statistics.snmp.context;

import net.paguo.statistics.snmp.commands.SnmpRunner;
import net.paguo.statistics.snmp.database.DBProxy;
import net.paguo.statistics.snmp.database.DBProxyFactory;
import net.paguo.statistics.snmp.dumper.Dumper;
import net.paguo.statistics.snmp.repositories.HostQuery;
import net.paguo.statistics.snmp.repositories.HostRepository;
import net.paguo.statistics.snmp.repositories.impl.HostRepositoryImpl;

public class RunContext {

    public DBProxy getDbProxy() {
        return DBProxyFactory.getDBProxy();
    }

    public HostRepository getHostRepository() {
        return new HostRepositoryImpl(getDbProxy());
    }

    public HostQuery getHostQuery() {
        return new HostQuery(getHostRepository());
    }

    public Dumper getDumper() {
        return new Dumper();
    }

    public SnmpRunner getSnmpRunner() {
        return new SnmpRunner();
    }

}
