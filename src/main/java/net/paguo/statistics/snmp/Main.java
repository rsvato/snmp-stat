package net.paguo.statistics.snmp;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import static org.apache.commons.io.FileUtils.writeLines;
import net.paguo.statistics.snmp.model.HostQuery;
import net.paguo.statistics.snmp.model.HostDefinition;
import net.paguo.statistics.snmp.model.HostResult;
import net.paguo.statistics.snmp.commands.HostCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.nio.file.Files;
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
    public static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        log.debug("Starting main thread. Getting hosts");
        long start = System.currentTimeMillis();
        HostQuery q = new HostQuery();
        Set<HostDefinition> hostDefinitions = q.getDefinitions();
        final List<Future<HostResult>> futures = new ArrayList<>();
        Map<String, HostCallable.RESULT> registry = new ConcurrentHashMap<>();

        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        for (HostDefinition hd : hostDefinitions) {
            final Future<HostResult> result = pool.submit(new HostCallable(hd, registry));
            futures.add(result);
        }

        log.debug("Shutting pool down");
        pool.shutdown();
        log.debug("Waiting tasks to finish");

        try {
            boolean terminated = pool.awaitTermination(30, TimeUnit.SECONDS);
            if (! terminated) {
                pool.shutdownNow();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }


        long runTime = System.currentTimeMillis() - start;
        log.debug("Collecting time: " + runTime);

        Collection<HostResult> results = new ArrayList<>();

        for (Future<HostResult> future : futures) {
            try {
                results.add(future.get(5, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("Error when getting results", e);
            }
        }

        results = results.stream().filter(HostResult::isValid).toList();

        try {
            q.saveInformation(results);
            dumpResults(results);
        } catch (SQLException | ConfigurationException e) {
            log.error("Error saving results", e);
        }
        log.debug("Finished.");
    }

    private static void dumpResults(Collection<HostResult> results) throws ConfigurationException, IOException {
        long time = System.currentTimeMillis();
        String dbprops = System.getProperty("dbprops");
        PropertiesConfiguration configuration = new PropertiesConfiguration();
        configuration.read(new FileReader(dbprops + ".properties"));
        String s = configuration.getString("dump.dir", Files.createTempDirectory("out-").toString());
        String filename = "dump-" + time;
        File f = new File(s.trim() + "/" + filename.trim());
        if (f.exists()){
            log.error("Dump file already exists");
            return;
        }
        Collection<String> lines = new ArrayList<>();
        for (HostResult result : results) {
            for (Long ifaceId : result.getInterfaces().keySet()) {
                String ifaceName = result.getInterfaces().get(ifaceId);
                String input = result.getInputs().get(ifaceId);
                String output = result.getOutputs().get(ifaceId);
                String cisco = result.getAddress().getHostAddress();
                String ifacePrint = ifaceName.replaceAll("\\W", "-");
                lines.add(String.format("%s|%s|%s|%s|%d", cisco, ifacePrint, input, output, time));
            }
        }
        writeLines(f, lines);

    }
}