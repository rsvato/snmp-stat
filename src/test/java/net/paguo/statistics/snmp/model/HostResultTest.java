package net.paguo.statistics.snmp.model;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class HostResultTest extends TestCase {

    private static Map<Long, String> sampleInterfaces() {
        Map<Long, String> m = new HashMap<>();
        m.put(1L, "eth0");
        return m;
    }

    private static Map<Long, String> sampleOctets() {
        Map<Long, String> m = new HashMap<>();
        m.put(1L, "1000");
        return m;
    }

    public void testIsValid_allSet_returnsTrue() {
        HostResult result = new HostResult("127.0.0.1");
        result.setUptime(1000);
        result.setInterfaces(sampleInterfaces());
        result.setInputs(sampleOctets());
        result.setOutputs(sampleOctets());

        assertTrue(result.isValid());
    }

    public void testIsValid_zeroUptime_returnsFalse() {
        HostResult result = new HostResult("127.0.0.1");
        result.setUptime(0);
        result.setInterfaces(sampleInterfaces());
        result.setInputs(sampleOctets());
        result.setOutputs(sampleOctets());

        assertFalse(result.isValid());
    }

    public void testIsValid_nullInterfaces_returnsFalse() {
        HostResult result = new HostResult("127.0.0.1");
        result.setUptime(1000);
        result.setInterfaces(null);
        result.setInputs(sampleOctets());
        result.setOutputs(sampleOctets());

        assertFalse(result.isValid());
    }

    public void testIsValid_emptyInterfaces_returnsFalse() {
        HostResult result = new HostResult("127.0.0.1");
        result.setUptime(1000);
        result.setInterfaces(new HashMap<>());
        result.setInputs(sampleOctets());
        result.setOutputs(sampleOctets());

        assertFalse(result.isValid());
    }

    public void testIsValid_nullInputs_returnsFalse() {
        HostResult result = new HostResult("127.0.0.1");
        result.setUptime(1000);
        result.setInterfaces(sampleInterfaces());
        result.setInputs(null);
        result.setOutputs(sampleOctets());

        assertFalse(result.isValid());
    }

    public void testIsValid_nullOutputs_returnsFalse() {
        HostResult result = new HostResult("127.0.0.1");
        result.setUptime(1000);
        result.setInterfaces(sampleInterfaces());
        result.setInputs(sampleOctets());
        result.setOutputs(null);

        assertFalse(result.isValid());
    }

    public void testIsValid_allNull_returnsFalse() {
        HostResult result = new HostResult("127.0.0.1");
        result.setUptime(0);

        assertFalse(result.isValid());
    }
}