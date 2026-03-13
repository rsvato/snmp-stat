package net.paguo.statistics.snmp.commands;

import net.paguo.statistics.snmp.commands.impl.DoubledRenameStrategyImpl;
import net.paguo.statistics.snmp.commands.packets.PacketBuilder;
import net.paguo.statistics.snmp.model.HostDefinition;
import net.paguo.statistics.snmp.model.HostResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
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
 */
public class SnmpHostProcessor {
    private static final Logger log = LoggerFactory.getLogger(SnmpHostProcessor.class);
    private static final long MAX_TIMEOUT = 2000L;

    public SnmpHostProcessor(){
    }

    public HostResult call(HostDefinition definition) {
        HostResult result = null;
        try {
            long start = System.currentTimeMillis();
            result = doQuery(definition.hostAddress(), definition.community());
            log.debug("Address {} session time: {} ms", definition.hostAddress(), System.currentTimeMillis() - start);
        } catch (IOException e) {
            log.error("Error getting result for definition {}", definition, e);
        }
        return result;
    }

    public HostResult doQuery(String host, String community) throws IOException {
        HostResult def = new HostResult(host);
        try (SnmpSession snmpSession = SnmpSession.newQuery(host, community, MAX_TIMEOUT)) {
            Long uptime = getUptime(snmpSession, host);
            if (uptime != null) {
                def.setUptime(uptime);
            }

            final Map<Long, String> rawInterfaces = snmpSession.getBulk(PacketBuilder.INTERFACES_OID);
            Map<Long, String> interfaces = checkInterfaces(rawInterfaces, host);
            Map<Long, String> inputs = snmpSession.getBulk(PacketBuilder.INCOME_OID);
            Map<Long, String> outputs = snmpSession.getBulk(PacketBuilder.OUTCOME_OID);
            def.setInterfaces(interfaces);
            def.setInputs(inputs);
            def.setOutputs(outputs);
        } catch (Exception e) {
            log.error("Unexpected exception during SNMP queries to {}", host, e);
        }
        return def;
    }

    private Long getUptime(SnmpSession snmp, String address) throws IOException {
        ResponseEvent evtx = snmp.send(PacketBuilder.uptimePDU());
        PDU response = evtx.getResponse();
        Long result = null;
        if (response != null && response.getErrorStatus() == 0){
            Vector<?> bindings = response.getVariableBindings();
            if (!bindings.isEmpty()) {
                VariableBinding binding = (VariableBinding) bindings.getFirst();
                Variable variable = binding.getVariable();
                result = variable.toLong();
            } else {
                log.error("Uptime request for {} returned no response", address);
            }
        } else {
            log.error("Uptime request failed for {}", address);
        }
        return result;
    }

    Map<Long,String> checkInterfaces(Map<Long, String> interfaces, String host) {
        Map<Long, String>  result = new HashMap<>();
        RenameStrategy doubleStrategy = new DoubledRenameStrategyImpl();
        Set<String> seen = countEntries(interfaces.values());
        for (Long interfaceIndex : interfaces.keySet()) {
           String interfaceName = interfaces.get(interfaceIndex);
           if (seen.contains(interfaceName)){
               final String value = doubleStrategy.renameInterface(interfaceName, interfaceIndex);
               result.put(interfaceIndex, value);
               log.debug("Host: {} already has interface {}", host, interfaceName);
           } else {
               result.put(interfaceIndex, interfaceName);
           }
        }
        return result;
    }

    private Set<String> countEntries(Collection<String> names){
        Map<String, Integer> result = new HashMap<>();

        for (String name : names) {
            result.compute(name, (k, count) -> count == null ? 1 : count + 1);
        }

        final Set<String> doubled = new HashSet<>();
        for (String s : result.keySet()) {
            Integer count = result.get(s);
            if (count > 1){
                doubled.add(s);
            }
        }
        return doubled;
    }
}

