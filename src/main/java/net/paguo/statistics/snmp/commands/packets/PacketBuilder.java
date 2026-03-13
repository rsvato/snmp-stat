package net.paguo.statistics.snmp.commands.packets;

import org.snmp4j.PDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

public class PacketBuilder {

    public static final OID INTERFACES_OID = new OID(".1.3.6.1.2.1.2.2.1.2");
    public static final OID INCOME_OID = new OID(".1.3.6.1.2.1.2.2.1.10");
    public static final OID OUTCOME_OID = new OID(".1.3.6.1.2.1.2.2.1.16");
    private static final OID UPTIME_OID = new OID(".1.3.6.1.2.1.1.3.0");

    public static PDU uptimePDU() {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(UPTIME_OID));
        pdu.setType(PDU.GET);
        return pdu;
    }

}
