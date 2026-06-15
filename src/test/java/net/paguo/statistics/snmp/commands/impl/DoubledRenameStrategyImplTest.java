package net.paguo.statistics.snmp.commands.impl;

import org.junit.Assert;
import org.junit.Test;

public class DoubledRenameStrategyImplTest {

    private final DoubledRenameStrategyImpl strategy = new DoubledRenameStrategyImpl();

    @Test
    public void testRenameInterface_basicName() {
        String result = strategy.renameInterface("eth0", 1L);
        Assert.assertEquals("eth0-ix1", result);
    }

    @Test
    public void testRenameInterface_longName() {
        String result = strategy.renameInterface("GigabitEthernet0/1", 5L);
        Assert.assertEquals("GigabitEthernet0/1-ix5", result);
    }

    @Test
    public void testRenameInterface_largeIndex() {
        String result = strategy.renameInterface("Gi0", 100L);
        Assert.assertEquals("Gi0-ix100", result);
    }

    @Test
    public void testRenameInterface_zeroIndex() {
        String result = strategy.renameInterface("vlan", 0L);
        Assert.assertEquals("vlan-ix0", result);
    }
}
