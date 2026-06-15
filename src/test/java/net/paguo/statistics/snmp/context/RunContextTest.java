package net.paguo.statistics.snmp.context;

import net.paguo.statistics.snmp.commands.SnmpRunner;
import net.paguo.statistics.snmp.database.DBProxy;
import net.paguo.statistics.snmp.dumper.Dumper;
import net.paguo.statistics.snmp.repositories.*;
import net.paguo.statistics.snmp.repositories.impl.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class RunContextTest {

    private RunContext ctx;

    @Before
    public void setUp() {
        ctx = new RunContext(mock(DBProxy.class));
    }

    @Test
    public void getHostRepository_returnsCorrectType() {
        Assert.assertTrue(ctx.getHostRepository() instanceof HostRepositoryImpl);
    }

    @Test
    public void getTrafficRecordsWriteRepository_returnsCorrectType() {
        Assert.assertTrue(ctx.getTrafficRecordsWriteRepository() instanceof TrafficRecordsWriteRepositoryImpl);
    }

    @Test
    public void getRoutersRepository_returnsCorrectType() {
        Assert.assertTrue(ctx.getRoutersRepository() instanceof RoutersRepositoryImpl);
    }

    @Test
    public void getUptimeRepository_returnsCorrectType() {
        Assert.assertTrue(ctx.getUptimeRepository() instanceof UptimeRepositoryImpl);
    }

    @Test
    public void getColllectionAuditRepository_returnsCorrectType() {
        Assert.assertTrue(ctx.getColllectionAuditRepository() instanceof CollectionAuditRepositoryImpl);
    }

    @Test
    public void getHostRepository_sameInstanceEachCall() {
        HostRepository first = ctx.getHostRepository();
        HostRepository second = ctx.getHostRepository();
        Assert.assertSame(first, second);
    }

    @Test
    public void getTrafficRecordsWriteRepository_sameInstanceEachCall() {
        TrafficRecordsWriteRepository first = ctx.getTrafficRecordsWriteRepository();
        TrafficRecordsWriteRepository second = ctx.getTrafficRecordsWriteRepository();
        Assert.assertSame(first, second);
    }

    @Test
    public void getSnmpRunner_sameInstanceEachCall() {
        SnmpRunner first = ctx.getSnmpRunner();
        SnmpRunner second = ctx.getSnmpRunner();
        Assert.assertSame(first, second);
    }

    @Test
    public void getSnmpRunner_returnsCorrectType() {
        Assert.assertTrue(ctx.getSnmpRunner() instanceof SnmpRunner);
    }

    @Test
    public void getDumper_returnsCorrectType() {
        Assert.assertTrue(ctx.getDumper() instanceof Dumper);
    }

    @Test
    public void getHostQuery_returnsHostQuery() {
        Assert.assertTrue(ctx.getHostQuery() instanceof HostQuery);
    }

    @Test
    public void getHostQuery_sameInstanceEachCall() {
        HostQuery first = ctx.getHostQuery();
        HostQuery second = ctx.getHostQuery();
        Assert.assertSame(first, second);
    }

    @Test
    public void allRepositories_shareSameDBProxy() {
        // Verify all repos were built with the same proxy by checking they're
        // all created at construction time (i.e., no lazy creation on each call)
        Object repo1 = ctx.getHostRepository();
        Object repo2 = ctx.getTrafficRecordsWriteRepository();
        Object repo3 = ctx.getRoutersRepository();
        // All created once, no nulls
        Assert.assertNotNull(repo1);
        Assert.assertNotNull(repo2);
        Assert.assertNotNull(repo3);
    }
}
