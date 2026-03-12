package net.paguo.statistics.snmp.commands;

import net.paguo.statistics.snmp.model.HostDefinition;
import net.paguo.statistics.snmp.model.HostResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public class SnmpRunner {

    private static final Logger log = LoggerFactory.getLogger(SnmpRunner.class);

    public List<HostResult> queryHostDefinitions(Set<HostDefinition> definitions) {
        long start = System.currentTimeMillis();
        final List<Future<HostResult>> futures = new ArrayList<>();

        try (ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2)) {
            for (HostDefinition hd : definitions) {
                final Future<HostResult> result = pool.submit(() -> new SnmpHostProcessor().call(hd));
                futures.add(result);
            }

            log.debug("Shutting pool down");
            pool.shutdown();
            log.debug("Waiting tasks to finish");

            try {
                boolean terminated = pool.awaitTermination(30, TimeUnit.SECONDS);
                if (!terminated) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
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

        return results.stream().filter(HostResult::isValid).toList();
    }

}
