package net.paguo.statistics.snmp.commands;

import net.paguo.statistics.snmp.model.HostDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

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

}
