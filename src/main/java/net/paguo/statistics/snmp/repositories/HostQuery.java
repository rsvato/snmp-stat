package net.paguo.statistics.snmp.repositories;

import net.paguo.statistics.snmp.model.HostDefinition;
import net.paguo.statistics.snmp.model.HostResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * User: slava
 * Date: 26.04.2007
 * Time: 0:18:17
 * Version: $Id$
 */
public class HostQuery {
    private static final Logger log = LoggerFactory.getLogger(HostQuery.class);

    private final HostRepository hostRepository;
    private final TrafficRecordsWriteRepository trafficRecordsWriteRepository;
    private final RoutersRepository routersRepository;
    private final UptimeRepository uptimeRepository;
    private final CollectionAuditRepository collectionAuditRepository;

    public HostQuery(HostRepository hostRepository,
                     TrafficRecordsWriteRepository trafficRecordsWriteRepository,
                     RoutersRepository routersRepository, UptimeRepository uptimeRepository,
                     CollectionAuditRepository colllectionAuditRepository) {
        this.hostRepository = hostRepository;
        this.trafficRecordsWriteRepository = trafficRecordsWriteRepository;
        this.routersRepository = routersRepository;
        this.uptimeRepository = uptimeRepository;
        this.collectionAuditRepository = colllectionAuditRepository;
    }

    public Set<HostDefinition> getDefinitions() {
        try {
            return hostRepository.allActive();
        } catch (Exception e) {
            log.error("DBError", e);
            return Collections.emptySet();
        }
    }

    public void saveInformation(Collection<HostResult> results) throws SQLException {
        Timestamp now = new Timestamp(new java.util.Date().getTime());
        int newInterfaces = routersRepository.saveInterfaces(results);
        log.info("Discovered {} new interfaces", newInterfaces);
        int written = trafficRecordsWriteRepository.saveTraffic(results, now);
        log.info("Saved {} traffic records", written);
        int uptimes = uptimeRepository.saveUptime(results, now);
        log.info("Saved {} uptime records", uptimes);
        int audits = collectionAuditRepository.writeAuditLog(results, now);
        log.info("Saved {} check records", audits);
    }
}
