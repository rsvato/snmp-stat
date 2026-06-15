package net.paguo.statistics.snmp.commands.packets;

import org.junit.Assert;
import org.junit.Test;
import org.snmp4j.PDU;
import org.snmp4j.smi.VariableBinding;

import java.util.List;

public class PacketBuilderTest {

    @Test
    public void testOidConstants_interfaces() {
        Assert.assertEquals("1.3.6.1.2.1.2.2.1.2", PacketBuilder.INTERFACES_OID.toString());
    }

    @Test
    public void testOidConstants_inOctets() {
        Assert.assertEquals("1.3.6.1.2.1.2.2.1.10", PacketBuilder.INCOME_OID.toString());
    }

    @Test
    public void testOidConstants_outOctets() {
        Assert.assertEquals("1.3.6.1.2.1.2.2.1.16", PacketBuilder.OUTCOME_OID.toString());
    }

    @Test
    public void testUptimePdu_typeIsGet() {
        PDU pdu = PacketBuilder.uptimePDU();
        Assert.assertEquals(PDU.GET, pdu.getType());
    }

    @Test
    public void testUptimePdu_singleBinding() {
        PDU pdu = PacketBuilder.uptimePDU();
        List<? extends VariableBinding> bindings = pdu.getVariableBindings();
        Assert.assertEquals(1, bindings.size());
    }

    @Test
    public void testUptimePdu_correctOid() {
        PDU pdu = PacketBuilder.uptimePDU();
        VariableBinding binding = pdu.getVariableBindings().getFirst();
        Assert.assertEquals("1.3.6.1.2.1.1.3.0", binding.getOid().toString());
    }

    @Test
    public void testUptimePdu_isolatedInstances() {
        PDU pdu1 = PacketBuilder.uptimePDU();
        PDU pdu2 = PacketBuilder.uptimePDU();
        Assert.assertNotSame(pdu1, pdu2);
        Assert.assertNotSame(pdu1.getVariableBindings(), pdu2.getVariableBindings());
    }
}
