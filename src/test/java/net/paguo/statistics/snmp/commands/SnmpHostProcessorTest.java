package net.paguo.statistics.snmp.commands;

import net.paguo.statistics.snmp.commands.packets.PacketBuilder;
import net.paguo.statistics.snmp.model.HostResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class SnmpHostProcessorTest {

    @Mock
    private SnmpSession session;

    private SnmpHostProcessor processor;

    private static final OID UPTIME_OID = new OID(".1.3.6.1.2.1.1.3.0");
    private static final IpAddress LOCALHOST = new IpAddress("127.0.0.1");

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        processor = new SnmpHostProcessor();
    }

    @Test
    public void doQuery_allResponsesValid_resultIsValid() throws IOException {
        when(session.send(any(PDU.class))).thenAnswer(inv -> uptimeEvent(12345L));
        when(session.getBulk(PacketBuilder.INTERFACES_OID)).thenReturn(Map.of(1L, "eth0", 2L, "eth1"));
        when(session.getBulk(PacketBuilder.INCOME_OID)).thenReturn(Map.of(1L, "100", 2L, "200"));
        when(session.getBulk(PacketBuilder.OUTCOME_OID)).thenReturn(Map.of(1L, "300", 2L, "400"));

        HostResult result = processor.doQuery("127.0.0.1", session);

        Assert.assertTrue(result.isValid());
        Assert.assertEquals(12345L, result.getUptime());
        Assert.assertEquals(2, result.getInterfaces().size());
        Assert.assertEquals(2, result.getInputs().size());
        Assert.assertEquals(2, result.getOutputs().size());
    }

    @Test
    public void doQuery_nullUptime_resultInvalid() throws IOException {
        when(session.send(any(PDU.class))).thenAnswer(inv -> nullResponse());
        when(session.getBulk(any(OID.class))).thenReturn(Map.of(1L, "eth0"));

        HostResult result = processor.doQuery("127.0.0.1", session);

        Assert.assertFalse(result.isValid());
        Assert.assertEquals(0, result.getUptime());
    }

    @Test
    public void doQuery_zeroUptime_resultInvalid() throws IOException {
        when(session.send(any(PDU.class))).thenAnswer(inv -> uptimeEvent(0L));
        when(session.getBulk(any(OID.class))).thenReturn(Map.of(1L, "eth0"));

        HostResult result = processor.doQuery("127.0.0.1", session);

        Assert.assertFalse(result.isValid());
        Assert.assertEquals(0L, result.getUptime());
    }

    @Test
    public void doQuery_emptyInterfaces_resultInvalid() throws IOException {
        when(session.send(any(PDU.class))).thenAnswer(inv -> uptimeEvent(1000L));
        when(session.getBulk(PacketBuilder.INTERFACES_OID)).thenReturn(Map.of());
        when(session.getBulk(PacketBuilder.INCOME_OID)).thenReturn(Map.of(1L, "100"));
        when(session.getBulk(PacketBuilder.OUTCOME_OID)).thenReturn(Map.of(1L, "200"));

        HostResult result = processor.doQuery("127.0.0.1", session);

        Assert.assertFalse(result.isValid());
        Assert.assertTrue(result.getInterfaces() == null || result.getInterfaces().isEmpty());
    }

    @Test
    public void doQuery_nullInputs_resultInvalid() throws IOException {
        when(session.send(any(PDU.class))).thenAnswer(inv -> uptimeEvent(1000L));
        when(session.getBulk(PacketBuilder.INTERFACES_OID)).thenReturn(Map.of(1L, "eth0"));
        when(session.getBulk(PacketBuilder.INCOME_OID)).thenReturn(null);
        when(session.getBulk(PacketBuilder.OUTCOME_OID)).thenReturn(Map.of(1L, "200"));

        HostResult result = processor.doQuery("127.0.0.1", session);

        Assert.assertFalse(result.isValid());
        Assert.assertNull(result.getInputs());
    }

    @Test
    public void doQuery_sessionThrows_resultInvalid() throws IOException {
        when(session.send(any(PDU.class))).thenThrow(new IOException("timeout"));

        HostResult result = processor.doQuery("127.0.0.1", session);

        Assert.assertFalse(result.isValid());
    }

    @Test
    public void doQuery_getBulkThrows_resultInvalid() throws IOException {
        when(session.send(any(PDU.class))).thenAnswer(inv -> uptimeEvent(1000L));
        when(session.getBulk(PacketBuilder.INTERFACES_OID)).thenThrow(new IOException("walk failed"));

        HostResult result = processor.doQuery("127.0.0.1", session);

        Assert.assertFalse(result.isValid());
    }

    @Test
    public void doQuery_deduplicatesInterfaceNames() throws IOException {
        when(session.send(any(PDU.class))).thenAnswer(inv -> uptimeEvent(5000L));
        when(session.getBulk(PacketBuilder.INTERFACES_OID))
                .thenReturn(Map.of(1L, "vlan", 2L, "vlan", 3L, "eth0"));
        when(session.getBulk(PacketBuilder.INCOME_OID)).thenReturn(Map.of(1L, "1", 2L, "2", 3L, "3"));
        when(session.getBulk(PacketBuilder.OUTCOME_OID)).thenReturn(Map.of(1L, "10", 2L, "20", 3L, "30"));

        HostResult result = processor.doQuery("127.0.0.1", session);

        Assert.assertTrue(result.isValid());
        Assert.assertEquals("vlan-ix1", result.getInterfaces().get(1L));
        Assert.assertEquals("vlan-ix2", result.getInterfaces().get(2L));
        Assert.assertEquals("eth0", result.getInterfaces().get(3L));
    }

    // --- helpers ---

    private ResponseEvent<IpAddress> uptimeEvent(long uptime) {
        PDU response = new PDU();
        response.add(new VariableBinding(UPTIME_OID, new TimeTicks(uptime)));
        response.setErrorStatus(0);
        return new ResponseEvent<>(this, LOCALHOST, null, response, null, 0);
    }

    private ResponseEvent<IpAddress> nullResponse() {
        return new ResponseEvent<>(this, LOCALHOST, null, null, null, 0);
    }
}
