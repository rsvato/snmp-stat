package net.paguo.statistics.snmp.model;

/**
 * User: slava
 * Date: 26.04.2007
 * Time: 0:17:29
 * Version: $Id$
 */
public class HostDefinition {
    private String hostAddress;
    private String community;

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }
}
