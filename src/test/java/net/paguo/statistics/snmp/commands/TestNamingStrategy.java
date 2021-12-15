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
        definition = new HostDefinition();
        definition.setCommunity("public");
        definition.setHostAddress("127.0.0.1");

        normalInterfaces = new HashMap<Long, String>();
        normalInterfaces.put(1l, "abc");
        normalInterfaces.put(2l, "def");
        normalInterfaces.put(3l, "ghi");

        doubledInterfaces = new HashMap<Long, String>();
        doubledInterfaces.put(1l, "foo");
        doubledInterfaces.put(2l, "foo");
        doubledInterfaces.put(3l, "foo");
        doubledInterfaces.put(4l, "foo");
    }


    @Test
    public void testNormalInterfaces(){
        HostCallable runner = new HostCallable(definition, null);
        Map<Long, String> ifs = runner.checkInterfaces(normalInterfaces);
        Assert.assertEquals("abc", ifs.get(1L));
        Assert.assertEquals("def", ifs.get(2L));
        Assert.assertEquals("ghi", ifs.get(3L));
    }

    @Test
    public void testDoubledInterfaces(){
       HostCallable runner = new HostCallable(definition, null);
       Map<Long, String> ifs = runner.checkInterfaces(doubledInterfaces);
       Assert.assertEquals("foo-ix1", ifs.get(1L));
       Assert.assertEquals("foo-ix2", ifs.get(2L));
       Assert.assertEquals("foo-ix3", ifs.get(3L));
       Assert.assertEquals("foo-ix4", ifs.get(4L));
       logger.info("All done");
    }

}
