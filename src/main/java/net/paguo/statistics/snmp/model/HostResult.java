package net.paguo.statistics.snmp.model;

import java.util.Map;

/**
 * User: sreentenko
 * Date: 31.01.2009
 * Time: 1:59:54
 */
public class HostResult {
    HostDefinition address;
    long uptime;
    Map<Long, String> interfaces;
    Map<Long, String> inputs;
    Map<Long, String> outputs;


    public HostResult(HostDefinition hd){
        this.address = hd;
    }

    public HostDefinition getAddress() {
        return address;
    }

    public void setAddress(HostDefinition address) {
        this.address = address;
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
}
