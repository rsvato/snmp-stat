package net.paguo.statistics.snmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.FileUtils.writeLines;
import net.paguo.statistics.snmp.model.HostQuery;
import net.paguo.statistics.snmp.model.HostDefinition;
import net.paguo.statistics.snmp.model.HostResult;
import net.paguo.statistics.snmp.commands.HostRunner;
import net.paguo.statistics.snmp.commands.HostCallable;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.io.File;
import java.io.IOException;

/**
 * Paraller query/sequential update implementation
 * User: slava
 * Date: 25.04.2007
 * Time: 23:45:11
 * Version: $Id$
 */
public class Main {
    public static final Log log = LogFactory.getLog(Main.class);

    public static void main(String args[]) throws SQLException, IOException {
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
            dumpResults(results);
        } catch (SQLException e) {
            log.error(e);
        } catch (ConfigurationException e) {
            log.error(e);
        }
        log.debug("Finished.");
    }

    private static void dumpResults(Collection<HostResult> results) throws ConfigurationException, IOException {
        long time = System.currentTimeMillis();
        Configuration configuration = new PropertiesConfiguration(System.getProperty("dbprops"));
        String s = configuration.getString("dump.dir");
        if (StringUtils.isEmpty(s)){
            log.error("Cannot find dump directory");
            return;
        }

        String filename = "dump-" + String.valueOf(time);
        File f = new File(s.trim() + "/" + filename.trim());
        if (f.exists()){
            log.error("Dump file already exists");
            return;
        }
        Collection<String> lines = new LinkedList<String>();
        for (HostResult result : results) {
            for (Long ifaceId : result.getInterfaces().keySet()) {
                String ifaceName = result.getInterfaces().get(ifaceId);
                String input = result.getInputs().get(ifaceId);
                String output = result.getOutputs().get(ifaceId);
                String cisco = result.getAddress().getHostAddress();
                String ifacePrint = ifaceName.replaceAll("\\W", "-");
                lines.add(String.format("%s|%s|%s|%s|%d", cisco, ifacePrint, input, output, time));
            }

            result.getInputs();
            result.getOutputs();
            result.getInterfaces();
        }
        writeLines(f, lines);

    }
}