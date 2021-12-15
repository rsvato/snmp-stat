package net.paguo.statistics.snmp.commands;

import net.paguo.statistics.snmp.commands.impl.DoubledRenameStrategyImpl;
import net.paguo.statistics.snmp.commands.impl.NormalRenameStrategyImpl;
import net.paguo.statistics.snmp.model.HostDefinition;
import net.paguo.statistics.snmp.model.HostQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Session;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * User: slava
 * Date: 26.04.2007
 * Time: 1:07:18
 * Version: $Id$
 * @deprecated
 */
@Deprecated
public class HostRunner implements Runnable{
    private static final Logger log = LoggerFactory.getLogger(HostRunner.class);
    private final HostDefinition definition;
    private static final long MAX_TIMEOUT = 2000l;
    private static final OID UPTIME_OID = new OID(".1.3.6.1.2.1.1.3.0");
    private static final OID INTERFACES_OID = new OID(".1.3.6.1.2.1.2.2.1.2");
    private static final OID INCOME_OID = new OID(".1.3.6.1.2.1.2.2.1.10");
    private static final OID OUTCOME_OID = new OID(".1.3.6.1.2.1.2.2.1.16");

    public HostRunner(HostDefinition hostDefinition){
        this.definition = hostDefinition;
    }

    public void run() {
        try {
            long start = System.currentTimeMillis();
            doQuery();
            long runningTime = System.currentTimeMillis() - start;
            log.debug("{} session time: {} ms", definition.getHostAddress(), runningTime);
        } catch (IOException e) {
            log.error("IOException during SNMP query", e);
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
        final Map<Long, String> map = getBulk(target, snmp, INTERFACES_OID);
        Map<Long, String> interfaces = checkInterfaces(map);

        Map<Long, String> inputs = getBulk(target, snmp, INCOME_OID);
        Map<Long, String> outputs = getBulk(target, snmp, OUTCOME_OID);
        snmp.close();
        analyze(hq, interfaces, inputs, outputs);
    }

    Map<Long,String> checkInterfaces(Map<Long, String> interfaces) {
        Map<Long, String>  result = new HashMap<Long, String>();
        RenameStrategy normalStrategy = new NormalRenameStrategyImpl();
        RenameStrategy doubleStrategy = new DoubledRenameStrategyImpl();
        Set<String> doubled = countEntries(interfaces.values());
        for (Long interfaceIndex : interfaces.keySet()) {
           String interfaceName = interfaces.get(interfaceIndex);
           if (doubled.contains(interfaceName)){
               final String value = doubleStrategy.renameInterface(interfaceName, interfaceIndex);
               result.put(interfaceIndex, value);
               log.debug(MessageFormat
                       .format("Host: {0} already has {1}", this.definition.getHostAddress(),
                       interfaceName));
           }else{
               final String value = normalStrategy.renameInterface(interfaceName, interfaceIndex);
               result.put(interfaceIndex, value);
           }
        }
        return result;
    }

    private Set<String> countEntries(Collection<String> names){
        Map<String, Integer> result = new HashMap<String, Integer>();

        for (String name : names) {
            Integer count = result.get(name);
            result.put(name, count == null ? 1 : count + 1);
        }

        final Set<String> doubled = new HashSet<String>();
        for (String s : result.keySet()) {
            Integer count = result.get(s);
            if (count > 1){
                doubled.add(s);
            }
        }
        return doubled;
    }
    
    public String renameInterface(Long interfaceIndex, String interfaceName) {
        return interfaceName
                       + "-ix" + String.valueOf(interfaceIndex);
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
