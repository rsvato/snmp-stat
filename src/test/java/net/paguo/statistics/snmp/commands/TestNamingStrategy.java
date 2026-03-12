package net.paguo.statistics.snmp.commands;

import net.paguo.statistics.snmp.model.HostDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Reyentenko
 */
public class TestNamingStrategy {

    private static Map<Long, String> normalInterfaces;
    private static Map<Long, String> doubledInterfaces;
    private static HostDefinition definition;

    private final static Logger logger = LoggerFactory.getLogger(TestNamingStrategy.class);

    @Before
    public void init(){
        definition = new HostDefinition("public", "127.0.0.1");

        normalInterfaces = new HashMap<>();
        normalInterfaces.put(1L, "abc");
        normalInterfaces.put(2L, "def");
        normalInterfaces.put(3L, "ghi");

        doubledInterfaces = new HashMap<>();
        doubledInterfaces.put(1L, "foo");
        doubledInterfaces.put(2L, "foo");
        doubledInterfaces.put(3L, "foo");
        doubledInterfaces.put(4L, "foo");
    }


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
       logger.info("All done");
    }

}
