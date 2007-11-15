package net.paguo.statistics.snmp.commands;

import net.paguo.statistics.snmp.model.HostDefinition;
import net.paguo.statistics.snmp.model.HostQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * User: slava
 * Date: 26.04.2007
 * Time: 1:07:18
 * Version: $Id$
 */
public class HostRunner implements Runnable{
    private static final Log log = LogFactory.getLog(HostRunner.class);
    private HostDefinition definition;
    private static final long MAX_TIMEOUT = 2000l;
    private static final OID UPTIME_OID = new OID(".1.3.6.1.2.1.1.3.0");
    private static final OID INTERFACES_OID = new OID(".1.3.6.1.2.1.2.2.1.2");
    private static final OID INCOME_OID = new OID(".1.3.6.1.2.1.2.2.1.10");
    private static final OID OUTCOME_OID = new OID(".1.3.6.1.2.1.2.2.1.16");

    public HostRunner(HostDefinition definition){
        this.definition = definition;
    }

    public void run() {
        try {
            long start = System.currentTimeMillis();
            doQuery();
            log.debug(definition.getHostAddress() + " session time: "
                    + (System.currentTimeMillis() - start) + " ms");
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void doQuery() throws IOException {
        HostQuery hq = new HostQuery();
        Address snmpAddress = createAddress();
        TransportMapping mapping = new DefaultUdpTransportMapping();
        CommunityTarget target = createCommunity(snmpAddress);
        Snmp snmp = new Snmp(mapping);
        snmp.listen();
        PDU pdu = uptimePDU();
        ResponseEvent evtx = snmp.send(pdu, target);
        PDU response = evtx.getResponse();
        
        if (response != null && response.getErrorStatus() == 0){
            Vector bindings = response.getVariableBindings();
            if (bindings.size() > 0) {
                VariableBinding binding = (VariableBinding) bindings.get(0);
                Variable variable = binding.getVariable();
                long l = variable.toLong();
                hq.saveUptime(definition.getHostAddress(), l);
            }
        }else {
            log.error("Time request failed for " + definition.getHostAddress());
            return;
        }
        Map<Long, String> interfaces = getBulk(target, snmp, INTERFACES_OID);
        Map<Long, String> inputs = getBulk(target, snmp, INCOME_OID);
        Map<Long, String> outputs = getBulk(target, snmp, OUTCOME_OID);
        snmp.close();
        analyze(hq, interfaces, inputs, outputs);
    }

    private void analyze(HostQuery hq, Map<Long, String> interfaces, Map<Long, String> inputs, Map<Long, String> outputs) {
        hq.saveInterfaces(definition.getHostAddress(), interfaces);
        hq.saveInformation(definition.getHostAddress(), interfaces, inputs, outputs);
    }

    private Map<Long, String> getBulk(Target target, Session snmp, OID base) throws IOException {
        Map<Long, String> results = new HashMap<Long, String>();
        VariableBinding binding = runGetNext(target, snmp, base);
        while(binding != null && child(base, binding.getOid())) {
            OID oid = binding.getOid();
            Long index = (long) oid.get(oid.size() - 1);
            String result = binding.getVariable().toString();
            results.put(index, result);
            binding = runGetNext(target, snmp, oid);
        }
        return results;
    }

    private boolean child(OID base, OID oid) {
        return oid.toString().startsWith(base.toString());
    }

    private VariableBinding runGetNext(Target target, Session snmp, OID oid) throws IOException {
        ResponseEvent evtx;
        PDU ifaPdu = new PDU();
        ifaPdu.add(new VariableBinding(oid));
        ifaPdu.setType(PDU.GETNEXT);
        evtx = snmp.send(ifaPdu, target);
        VariableBinding binding = null;
        PDU ifacesResponse = evtx.getResponse();
        if (ifacesResponse != null && ifacesResponse.getErrorStatus() == 0){
            Vector bindings = ifacesResponse.getVariableBindings();
            binding = (VariableBinding) bindings.get(0);
        }
        return binding;
    }

    private PDU uptimePDU() {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(UPTIME_OID));
        pdu.setType(PDU.GET);
        return pdu;
    }

    private Address createAddress() {
        return GenericAddress.parse("udp:"
				+ definition.getHostAddress() + "/161");
    }

    private CommunityTarget createCommunity(Address snmpAddress) {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(definition.getCommunity()));
        target.setAddress(snmpAddress);
        target.setRetries(2);
        target.setTimeout(MAX_TIMEOUT);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }
}
