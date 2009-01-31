package net.paguo.statistics.snmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.paguo.statistics.snmp.model.HostQuery;
import net.paguo.statistics.snmp.model.HostDefinition;
import net.paguo.statistics.snmp.model.HostResult;
import net.paguo.statistics.snmp.commands.HostRunner;
import net.paguo.statistics.snmp.commands.HostCallable;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Paraller query/sequential update implementation
 * User: slava
 * Date: 25.04.2007
 * Time: 23:45:11
 * Version: $Id$
 */
public class Main {
    public static final Log log = LogFactory.getLog(Main.class);

    public static void main(String args[]) throws SQLException {
        log.debug("Starting main thread. Getting hosts");
        long start = System.currentTimeMillis();
        HostQuery q = new HostQuery();
        Set<HostDefinition> hostDefinitions = q.getDefinitions();

        final List<Future<HostResult>> futures = new LinkedList<Future<HostResult>>();

        Map<String, HostCallable.RESULT> registry = new ConcurrentHashMap<String, HostCallable.RESULT>();

        ExecutorService pool = Executors.newFixedThreadPool(hostDefinitions.size());
        for (HostDefinition hd : hostDefinitions) {
            final Future<HostResult> result = pool.submit(new HostCallable(hd, registry));
            futures.add(result);
        }
        log.debug("Shutting service down");
        pool.shutdown();
        log.debug("Waiting tasks to finish");
        try {
            int i = 0;
            // Wait a while for existing tasks to terminate
            do {
                log.debug("Awaiting termination");
                Thread.sleep(600 - i);
                i = 1 + 10;
                if (i >= 600){
                    log.debug("Resetting timer to zero");
                    i = 0;
                }
            } while(registry.size() < futures.size());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        long runTime = System.currentTimeMillis() - start;
        log.debug("Collecting time: " + runTime);
        Collection<HostResult> results = new LinkedList<HostResult>();
        for (Future<HostResult> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException e) {
                log.error(e);
            } catch (ExecutionException e) {
                log.error(e);
            }
        }
        for (Iterator<HostResult> it = results.iterator(); it.hasNext();) {
            HostResult result = it.next();
            final boolean hasInterfaces = result.getInterfaces() != null && !result.getInterfaces().isEmpty();
            final boolean hasInputs = result.getInputs() != null && !result.getInputs().isEmpty();
            final boolean hasOutputs = result.getOutputs() != null && !result.getOutputs().isEmpty();
            final boolean hasUptime = result.getUptime() > 0;
            if (!hasUptime || !hasInterfaces || !hasInputs || !hasOutputs) {
                log.error(result.getAddress().getHostAddress() + " does not have required information, hence will not" +
                        " be updated");
                it.remove();
            }
        }

        try {
            q.saveInformation(results);
        } catch (SQLException e) {
            log.error(e);
        }
        log.debug("Finished.");
    }
}