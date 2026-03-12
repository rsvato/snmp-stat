package net.paguo.statistics.snmp.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SnmpSession implements AutoCloseable {
    private final long timeout;
    private Session snmp;
    private final Target target;

    private static final Logger log = LoggerFactory.getLogger(SnmpSession.class);

    private SnmpSession(String host, String community, long timeout) {
        this.timeout = timeout;
        this.target = createCommunity(createAddress(host), community);
    }

    private Address createAddress(String address) {
        return GenericAddress.parse("udp:" + address + "/161");
    }

    private void start() throws IOException {
        snmp = new Snmp(new DefaultUdpTransportMapping());
        ((Snmp) snmp).listen();
    }

    private CommunityTarget createCommunity(Address snmpAddress, String community) {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(snmpAddress);
        target.setRetries(2);
        target.setTimeout(timeout);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

    public static SnmpSession newQuery(String host, String community, long timeout) throws IOException {
        SnmpSession query = new SnmpSession(host, community, timeout);
        query.start();
        return query;
    }

    @Override
    public void close() throws Exception {
        if (snmp != null) {
            snmp.close();
        }
    }


    public ResponseEvent send(PDU pdu) throws IOException {
        return snmp.send(pdu, target);
    }

    public Map<Long, String> getBulk(OID base) throws IOException {
        Map<Long, String> results = new HashMap<>();
        VariableBinding binding = runGetNext(base);
        while(binding != null && ! binding.isException() && child(base, binding.getOid())) {
            OID oid = binding.getOid();
            Long index = (long) oid.get(oid.size() - 1);
            String result = binding.getVariable().toString();
            results.put(index, result);
            binding = runGetNext(oid);
            if (binding == null || binding.isException()) {
                log.error("GETNEXT gives exceptional or no result for OID {}", oid);
                break;
            }
        }
        return results;
    }

    private VariableBinding runGetNext(OID oid) throws IOException {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GETNEXT);
        ResponseEvent responseEvent = send(pdu);
        VariableBinding binding = null;
        PDU response = responseEvent.getResponse();
        if (response != null && response.getErrorStatus() == 0){
            Vector<?> bindings = response.getVariableBindings();
            binding = (VariableBinding) bindings.getFirst();
        }
        return binding;
    }


    private boolean child(OID base, OID oid) {
        return oid.toString().startsWith(base.toString());
    }
}
