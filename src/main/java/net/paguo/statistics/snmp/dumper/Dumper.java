package net.paguo.statistics.snmp.dumper;

import net.paguo.statistics.snmp.model.HostResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

public class Dumper {
    private static final Logger log = LoggerFactory.getLogger(Dumper.class);

    private final String dumpDir;

    public Dumper(String dumpDir) {
        this.dumpDir = dumpDir;
    }

    public void dumpResults(Collection<HostResult> results) throws IOException {
        long time = System.currentTimeMillis();
        String dir = dumpDir != null ? dumpDir : Files.createTempDirectory("out-").toString();
        String filename = "dump-" + time;
        File f = new File(dir.trim(), filename.trim());
        if (f.exists()) {
            log.error("Dump file already exists");
            return;
        }

        log.info("Dumping traffic information to file {}", f.getAbsolutePath());
        try (BufferedWriter writer = Files.newBufferedWriter(f.toPath())) {
            for (HostResult result : results) {
                for (Long ifaceId : result.getInterfaces().keySet()) {
                    String line = formatLine(result, ifaceId, time);
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }

    /** Format a single interface line. Package-private for testing. */
    String formatLine(HostResult result, Long ifaceId, long time) {
        String ifaceName = result.getInterfaces().get(ifaceId);
        String input = result.getInputs().get(ifaceId);
        String output = result.getOutputs().get(ifaceId);
        String cisco = result.getHostAddress();
        String ifacePrint = ifaceName.replaceAll("\\W", "-");
        return String.format("%s|%s|%s|%s|%d", cisco, ifacePrint, input, output, time);
    }
}
