package net.paguo.statistics.snmp.commands;

import net.paguo.statistics.snmp.model.HostDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

/**
 * @author Reyentenko
 */
public class TestNamingStrategy {

    private final Map<Long, String> normalInterfaces = Map.of(
            1L, "abc",
            2L, "def",
            3L, "ghi"
    );
    private final Map<Long, String> doubledInterfaces = Map.of(
            1L, "foo",
            2L, "foo",
            3L, "foo",
            4L, "foo"
    );
    private final HostDefinition definition = new HostDefinition("127.0.0.1", "public");

    @Test
    public void testNormalInterfaces(){
        SnmpHostProcessor runner = new SnmpHostProcessor();
        Map<Long, String> ifs = runner.checkInterfaces(normalInterfaces, definition.hostAddress());
        Assert.assertEquals("abc", ifs.get(1L));
        Assert.assertEquals("def", ifs.get(2L));
        Assert.assertEquals("ghi", ifs.get(3L));
    }

    @Test
    public void testDoubledInterfaces(){
       SnmpHostProcessor runner = new SnmpHostProcessor();
       Map<Long, String> ifs = runner.checkInterfaces(doubledInterfaces, definition.hostAddress());
       Assert.assertEquals("foo-ix1", ifs.get(1L));
       Assert.assertEquals("foo-ix2", ifs.get(2L));
       Assert.assertEquals("foo-ix3", ifs.get(3L));
       Assert.assertEquals("foo-ix4", ifs.get(4L));
    }

    @Test
    public void testEmptyInterfaces() {
        SnmpHostProcessor runner = new SnmpHostProcessor();
        Map<Long, String> ifs = runner.checkInterfaces(Map.of(), definition.hostAddress());
        Assert.assertTrue(ifs.isEmpty());
    }

    @Test
    public void testSingleInterface() {
        SnmpHostProcessor runner = new SnmpHostProcessor();
        Map<Long, String> ifs = runner.checkInterfaces(Map.of(5L, "eth0"), definition.hostAddress());
        Assert.assertEquals("eth0", ifs.get(5L));
        Assert.assertEquals(1, ifs.size());
    }

    @Test
    public void testPartialDuplicates() {
        SnmpHostProcessor runner = new SnmpHostProcessor();
        Map<Long, String> ifs = runner.checkInterfaces(
                Map.of(1L, "eth0", 2L, "eth0", 3L, "wlan0"),
                definition.hostAddress()
        );
        Assert.assertEquals("eth0-ix1", ifs.get(1L));
        Assert.assertEquals("eth0-ix2", ifs.get(2L));
        Assert.assertEquals("wlan0", ifs.get(3L));
        Assert.assertEquals(3, ifs.size());
    }

    @Test
    public void testDuplicatePreservesIndexKeys() {
        SnmpHostProcessor runner = new SnmpHostProcessor();
        Map<Long, String> ifs = runner.checkInterfaces(
                Map.of(10L, "Gi0", 20L, "Gi0"),
                definition.hostAddress()
        );
        Assert.assertEquals("Gi0-ix10", ifs.get(10L));
        Assert.assertEquals("Gi0-ix20", ifs.get(20L));
        Assert.assertTrue(ifs.keySet().containsAll(Set.of(10L, 20L)));
    }

}
