package net.paguo.statistics.snmp.model;

import java.util.Map;

/**
 * User: sreentenko
 * Date: 31.01.2009
 * Time: 1:59:54
 */
public class HostResult {
    private final String hostAddress;
    private long uptime;
    private Map<Long, String> interfaces;
    private Map<Long, String> inputs;
    private Map<Long, String> outputs;


    public HostResult(String hostAddress){
        this.hostAddress = hostAddress;
    }


    public String getHostAddress() {
        return hostAddress;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }


    public Map<Long, String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Map<Long, String> interfaces) {
        this.interfaces = interfaces;
    }

    public Map<Long, String> getInputs() {
        return inputs;
    }

    public void setInputs(Map<Long, String> inputs) {
        this.inputs = inputs;
    }

    public Map<Long, String> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<Long, String> outputs) {
        this.outputs = outputs;
    }

    public boolean isValid() {
        boolean hasInterfaces = getInterfaces() != null && !getInterfaces().isEmpty();
        boolean hasInputs = getInputs() != null && !getInputs().isEmpty();
        boolean hasOutputs = getOutputs() != null && !getOutputs().isEmpty();
        boolean hasUptime = getUptime() > 0;
        return hasUptime && hasInterfaces && hasInputs && hasOutputs;
    }
}
