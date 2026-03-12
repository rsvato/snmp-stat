package net.paguo.statistics.snmp.dumper;

import net.paguo.statistics.snmp.model.HostResult;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

import static org.apache.commons.io.FileUtils.writeLines;

public class Dumper {
    private static final Logger log = LoggerFactory.getLogger(Dumper.class);

    public void dumpResults(Collection<HostResult> results) throws ConfigurationException, IOException {
        long time = System.currentTimeMillis();
        PropertiesConfiguration configuration = getPropertiesConfiguration();
        String dumpDirectory = configuration.getString("dump.dir", Files.createTempDirectory("out-").toString());
        String filename = "dump-" + time;
        File f = new File(dumpDirectory.trim(), filename.trim());
        if (f.exists()){
            log.error("Dump file already exists");
            return;
        }
        Collection<String> lines = new ArrayList<>();
        log.info("Dumping traffic information to file {}", f.getAbsolutePath());
        for (HostResult result : results) {
            for (Long ifaceId : result.getInterfaces().keySet()) {
                String ifaceName = result.getInterfaces().get(ifaceId);
                String input = result.getInputs().get(ifaceId);
                String output = result.getOutputs().get(ifaceId);
                String cisco = result.getHostAddress();
                String ifacePrint = ifaceName.replaceAll("\\W", "-");
                lines.add(String.format("%s|%s|%s|%s|%d", cisco, ifacePrint, input, output, time));
            }
        }
        writeLines(f, lines);

    }

    private PropertiesConfiguration getPropertiesConfiguration() throws ConfigurationException, IOException {
        String dbprops = System.getProperty("dbprops");
        PropertiesConfiguration configuration = new PropertiesConfiguration();
        File f  = new File(dbprops + ".properties");
        if (f.exists()) {
            configuration.read(new FileReader(f));
        }
        return configuration;
    }

}
