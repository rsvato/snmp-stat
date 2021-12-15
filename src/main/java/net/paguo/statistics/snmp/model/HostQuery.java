package net.paguo.statistics.snmp.model;

import net.paguo.statistics.snmp.database.DBProxy;
import net.paguo.statistics.snmp.database.DBProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: slava
 * Date: 26.04.2007
 * Time: 0:18:17
 * Version: $Id$
 */
public class HostQuery {
    private static final String QUERY = "select address, community from snmp_addresses where is_active";
    private static final String SAVE_UPTIME = "insert into uptime(dt, uptime, cisco) values(?, ?, ?)";
    private static final String FIND_INTERFACE = "select id from cisco_iface where cisco = ? and interface = ?";
    private static final String ADD_INTERFACE = "insert into cisco_iface (cisco, interface) values (?, ?)";
    private static final String INSERT_TRAFFIC = "insert into tr(cisco, interface, inoctets, outoctets, dt) " +
            "values (?, ?, ?, ?, ?)";
    private static final String CHECK_TRAFFIC = "select count(*) from tr where cisco = ? and interface =? and dt = ?";


    private static final Logger log = LoggerFactory.getLogger(HostQuery.class);
    private static final String LAST_CHECK_SAVE = "insert into last_snmp_checks (cisco, last_check) values (?, ?)";

    public boolean checkTrafficRecordExists(String hostAddress, String iface, Timestamp now){
        log.debug("checkTrafficRecordExists() <<<");
        DBProxy proxy = DBProxyFactory.getDBProxy();
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            c = proxy.getConnection();
            ps = c.prepareStatement(CHECK_TRAFFIC);
            ps.setString(1, hostAddress);
            ps.setString(2, iface);
            ps.setTimestamp(3, now);
            rs = ps.executeQuery();
            if (rs.next()){
                result = rs.getInt(1) > 1;
            }
        } catch (SQLException e) {
            log.error("DBError", e);
        } finally {
            closeAll(c, ps, rs);
        }
        return result;
    }

    public Set<HostDefinition> getDefinitions() {
        log.debug("getHostList() " + "<<<");
        DBProxy proxy = DBProxyFactory.getDBProxy();
        Set<HostDefinition> result = new HashSet<HostDefinition>();
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = proxy.getConnection();
            ps = c.prepareStatement(QUERY);
            rs = ps.executeQuery();
            while (rs.next()) {
                HostDefinition element = new HostDefinition();
                element.setHostAddress(rs.getString(1));
                element.setCommunity(rs.getString(2));
                result.add(element);
            }
        } catch (SQLException e) {
            log.error("DBError", e);
        } finally {
            closeAll(c, ps, rs);
        }
        log.debug("getHostList() " + ">>>");
        return result;
    }

    public void saveUptime(String address, long uptime) {
        log.debug("saveUptime() " + "<<<");
        DBProxy proxy = DBProxyFactory.getDBProxy();
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = proxy.getConnection();
            ps = c.prepareStatement(SAVE_UPTIME);
            ps.setTimestamp(1, new Timestamp(new java.util.Date().getTime()));
            ps.setLong(2, uptime);
            ps.setString(3, address);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("DBError", e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException e) {
                log.error("DBError", e);
            }
        }
        log.debug("saveUptime() " + ">>>");
    }

    private Long getInterfaceId(String hostAddress, String iface){
        DBProxy proxy = DBProxyFactory.getDBProxy();
        Long result = null;
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = proxy.getConnection();
            ps = c.prepareStatement(FIND_INTERFACE);
            ps.setString(1, hostAddress);
            ps.setString(2, iface);
            rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getLong(1);
            }
        } catch (SQLException e) {
            log.error("DBError", e);
        } finally {
            closeAll(c, ps, rs);
        }
        return result;
    }

    private void closeAll(Connection connection, PreparedStatement preparedStatement) {
        closeAll(connection, preparedStatement, null);
    }

    private void closeAll(Connection c, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (c != null) {
                c.close();
            }
        } catch (SQLException e) {
            log.error("DBError", e);
        }
    }

    public void saveInterfaces(String hostAddress, Map<Long, String> interfaces) {
        log.debug("saveInterfaces(): <<<");
        for (String iface : interfaces.values()) {
            Long interfaceId = getInterfaceId(hostAddress, iface);
            if (interfaceId == null){
                log.debug(MessageFormat.format("Adding interface {0} for device {1}", iface, hostAddress));
                addInterface(hostAddress, iface);
            }
        }
        log.debug("saveInterfaces(): >>>");

    }

    private void addInterface(String hostAddress, String iface) {
        DBProxy proxy = DBProxyFactory.getDBProxy();
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = proxy.getConnection();
            ps = c.prepareStatement(ADD_INTERFACE);
            ps.setString(1, hostAddress);
            ps.setString(2, iface);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("DBError", e);
        } finally {
            closeAll(c, ps);
        }
    }



    public void saveInformation(Collection<HostResult> results) throws SQLException {
        //stage one: save interfaces
        DBProxy proxy = DBProxyFactory.getDBProxy();
        Connection c = proxy.getConnection();
        saveInterfaces(results, c);
        //save uptimes
        saveUptime(results, c);
        //stage two: save inputs and outputs
        log.debug("saveTraffic: >>>>");
        int[] results1 = saveTraffic(results, c);
        log.debug(results1.length + " records inserted for traffic total");
        log.debug("saveTraffic:>>>>>");
        c.close();
        final long time = new java.util.Date().getTime();
        for (HostResult result : results) {
            saveLastCheck(result.getAddress().getHostAddress(), new Timestamp(time));
        }
    }

    private void saveInterfaces(Collection<HostResult> results, Connection c) throws SQLException {
        log.debug("saveInterfaces(): <<<<");
        PreparedStatement psIf = c.prepareStatement(ADD_INTERFACE);

        for (HostResult result : results) {
            String hostAddress = result.getAddress().getHostAddress();
            for (String iface : result.getInterfaces().values()) {
                Long interfaceId = getInterfaceId(hostAddress, iface);
                if (interfaceId == null){
                    log.debug(MessageFormat.format("Adding interface {0} for device {1}", iface, hostAddress));
                    psIf.setString(1, hostAddress);
                    psIf.setString(2, iface);
                    psIf.addBatch();
                }
            }
        }
        psIf.executeBatch();
        psIf.close();
        log.debug("saveInterfaces(): done >>>");
    }

    private void saveUptime(Collection<HostResult> results, Connection c) throws SQLException {
        log.debug("saveUptime() " + "<<<");
        PreparedStatement psUptime = null;
        psUptime = c.prepareStatement(SAVE_UPTIME);
        for (HostResult result : results) {
            psUptime.setTimestamp(1, new Timestamp(new java.util.Date().getTime()));
            psUptime.setLong(2, result.getUptime());
            psUptime.setString(3, result.getAddress().getHostAddress());
            psUptime.addBatch();
        }
        int[] r = psUptime.executeBatch();
        log.debug("saveUptime() finished successfully for " + r.length + "records. >>>");
    }

    private int[] saveTraffic(Collection<HostResult> results, Connection c) throws SQLException {
        PreparedStatement psTraffic = c.prepareStatement(INSERT_TRAFFIC);
        for (HostResult result : results) {
            String hostAddress = result.getAddress().getHostAddress();
            Map<Long, String> interfaces = result.getInterfaces();
            Timestamp now = new Timestamp(new java.util.Date().getTime());
            Set<Long> indexes = interfaces.keySet();
            for (Long index : indexes) {
                String iface = interfaces.get(index);
                Long incoming = Long.parseLong(result.getInputs().get(index));
                Long outcoming = Long.parseLong(result.getOutputs().get(index));
                if (!checkTrafficRecordExists(hostAddress, iface, now)) {
                    psTraffic.setString(1, hostAddress);
                    psTraffic.setString(2, iface);
                    psTraffic.setLong(3, incoming);
                    psTraffic.setLong(4, outcoming);
                    psTraffic.setTimestamp(5, now);
                    psTraffic.addBatch();
                }
            }
        }
        int[] results1 = psTraffic.executeBatch();
        psTraffic.close();
        return results1;
    }

    public void saveInformation(String hostAddress, Map<Long, String> interfaces,
                                Map<Long, String> inputs, Map<Long, String> outputs){
        DBProxy proxy = DBProxyFactory.getDBProxy();
        Connection c = null;
        PreparedStatement ps = null;

        try {
            c = proxy.getConnection();
            ps = c.prepareStatement(INSERT_TRAFFIC);
            Timestamp now = new Timestamp(new java.util.Date().getTime());
            Set<Long> indexes = interfaces.keySet();
            for (Long index : indexes){
                String iface = interfaces.get(index);
                Long incoming = Long.parseLong(inputs.get(index));
                Long outcoming = Long.parseLong(outputs.get(index));
                if  (! checkTrafficRecordExists(hostAddress, iface, now)){
                    ps.setString(1, hostAddress);
                    ps.setString(2, iface);
                    ps.setLong(3, incoming);
                    ps.setLong(4, outcoming);
                    ps.setTimestamp(5, now);
                    ps.addBatch();
                }
            }
            int[] results  = ps.executeBatch();
            saveLastCheck(hostAddress, now);
            log.debug(results.length + " records inserted for address " + hostAddress);
        } catch (SQLException e) {
            log.error(hostAddress + " " + e);
        } finally {
            closeAll(c, ps, null);
        }
    }

    private void saveLastCheck(String hostAddress, Timestamp now) {
        DBProxy proxy = DBProxyFactory.getDBProxy();
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = proxy.getConnection();
            ps = c.prepareStatement(LAST_CHECK_SAVE);
            ps.setString(1, hostAddress);
            ps.setTimestamp(2, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error(hostAddress + " cannot save last check time " + e.getMessage());
        } finally {
            closeAll(c, ps, null);
        }
    }
}
