package net.paguo.statistics.snmp.repositories;

import net.paguo.statistics.snmp.database.DBProxy;
import net.paguo.statistics.snmp.database.DBProxyFactory;
import net.paguo.statistics.snmp.model.HostDefinition;
import net.paguo.statistics.snmp.model.HostResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * User: slava
 * Date: 26.04.2007
 * Time: 0:18:17
 * Version: $Id$
 */
public class HostQuery {
    private final HostRepository hostRepository;
    private final TrafficRecordsReadRepository trafficRecordsReadRepository;
    private final RoutersRepository routersRepository;
    private static final String SAVE_UPTIME = "insert into uptime(dt, uptime, cisco) values(?, ?, ?)";
    private static final String INSERT_TRAFFIC = "insert into tr(cisco, interface, inoctets, outoctets, dt) " +
            "values (?, ?, ?, ?, ?)";


    private static final Logger log = LoggerFactory.getLogger(HostQuery.class);
    private static final String LAST_CHECK_SAVE = "insert into last_snmp_checks (cisco, last_check) values (?, ?)";

    public HostQuery(HostRepository hostRepository,
                     TrafficRecordsReadRepository trafficRecordsReadRepository,
                     RoutersRepository routersRepository) {
        this.hostRepository = hostRepository;
        this.trafficRecordsReadRepository = trafficRecordsReadRepository;
        this.routersRepository = routersRepository;
    }

    public boolean checkTrafficRecordExists(String hostAddress, String iface, Timestamp now){
        try {
            return trafficRecordsReadRepository.checkTrafficRecordExists(hostAddress, iface, now);
        } catch (SQLException e) {
            log.error("Cannot check if record exists for {}, {} and {}", hostAddress, iface, now, e);
            return false;
        }
    }

    public Set<HostDefinition> getDefinitions() {
        try {
            return hostRepository.allActive();
        } catch (Exception e) {
            log.error("DBError", e);
            return Collections.emptySet();
        }
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



    public void saveInformation(Collection<HostResult> results) throws SQLException {
        //stage one: save interfaces
        DBProxy proxy = DBProxyFactory.getDBProxy();
        Connection c = proxy.getConnection();
        int newInterfaces = routersRepository.saveInterfaces(results);
        log.info("Processed {} interfaces", newInterfaces);
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
            saveLastCheck(result.getHostAddress(), new Timestamp(time));
        }
    }

    private void saveUptime(Collection<HostResult> results, Connection c) throws SQLException {
        log.debug("saveUptime() " + "<<<");
        PreparedStatement psUptime;
        psUptime = c.prepareStatement(SAVE_UPTIME);
        for (HostResult result : results) {
            psUptime.setTimestamp(1, new Timestamp(new java.util.Date().getTime()));
            psUptime.setLong(2, result.getUptime());
            psUptime.setString(3, result.getHostAddress());
            psUptime.addBatch();
        }
        int[] r = psUptime.executeBatch();
        log.debug("saveUptime() finished successfully for " + r.length + "records. >>>");
    }

    private int[] saveTraffic(Collection<HostResult> results, Connection c) throws SQLException {
        PreparedStatement psTraffic = c.prepareStatement(INSERT_TRAFFIC);
        for (HostResult result : results) {
            String hostAddress = result.getHostAddress();
            Map<Long, String> interfaces = result.getInterfaces();
            Timestamp now = new Timestamp(new java.util.Date().getTime());
            Set<Long> indexes = interfaces.keySet();
            for (Long index : indexes) {
                String iface = interfaces.get(index);
                long incoming = Long.parseLong(result.getInputs().get(index));
                long outcoming = Long.parseLong(result.getOutputs().get(index));
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
            log.error("Cannot save last check time for host {}", hostAddress, e);
        } finally {
            closeAll(c, ps, null);
        }
    }
}
