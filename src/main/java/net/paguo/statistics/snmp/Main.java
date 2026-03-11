package net.paguo.statistics.snmp;

import net.paguo.statistics.snmp.context.RunContext;
import org.apache.commons.configuration2.ex.ConfigurationException;

import net.paguo.statistics.snmp.repositories.HostQuery;
import net.paguo.statistics.snmp.model.HostDefinition;
import net.paguo.statistics.snmp.model.HostResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;

/**
 * Paraller query/sequential update implementation
 * User: slava
 * Date: 25.04.2007
 * Time: 23:45:11
 * Version: $Id$
 */
public class Main {
    public static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        log.debug("Starting main thread. Getting hosts");
        RunContext ctx = new RunContext();
        HostQuery q = ctx.getHostQuery();
        Set<HostDefinition> hostDefinitions = q.getDefinitions();
        List<HostResult> hostResults = ctx.getSnmpRunner().queryHostDefinitions(hostDefinitions);
        try {
            q.saveInformation(hostResults);
            ctx.getDumper().dumpResults(hostResults);
        } catch (SQLException | ConfigurationException e) {
            log.error("Error saving results", e);
        }
        log.debug("Finished.");
    }
}