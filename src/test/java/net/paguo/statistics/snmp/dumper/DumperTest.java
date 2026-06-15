package net.paguo.statistics.snmp.dumper;

import net.paguo.statistics.snmp.model.HostResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class DumperTest {

    private Path tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("dumper-test-");
    }

    @After
    public void tearDown() throws IOException {
        Files.walk(tempDir)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
    }

    private Dumper dumper() {
        return new Dumper(tempDir.toString());
    }

    @Test
    public void formatLine_basicInterface() {
        Dumper dumper = new Dumper(null);
        HostResult result = makeResult("10.0.0.1", 1000, Map.of(1L, "eth0"), Map.of(1L, "100"), Map.of(1L, "200"));

        String line = dumper.formatLine(result, 1L, 1700000000000L);

        Assert.assertEquals("10.0.0.1|eth0|100|200|1700000000000", line);
    }

    @Test
    public void formatLine_sanitizesSpecialChars() {
        Dumper dumper = new Dumper(null);
        HostResult result = makeResult("10.0.0.1", 1000,
                Map.of(1L, "GigabitEthernet0/1"),
                Map.of(1L, "500"), Map.of(1L, "600"));

        String line = dumper.formatLine(result, 1L, 1700000000000L);

        Assert.assertEquals("10.0.0.1|GigabitEthernet0-1|500|600|1700000000000", line);
    }

    @Test
    public void formatLine_sanitizesSpacesAndParens() {
        Dumper dumper = new Dumper(null);
        HostResult result = makeResult("10.0.0.1", 1000,
                Map.of(2L, "Serial (primary)"),
                Map.of(2L, "10"), Map.of(2L, "20"));

        String line = dumper.formatLine(result, 2L, 1700000000000L);

        Assert.assertEquals("10.0.0.1|Serial--primary-|10|20|1700000000000", line);
    }

    @Test
    public void dumpResults_singleHostSingleInterface() throws Exception {
        HostResult result = makeResult("10.0.0.1", 1000,
                Map.of(1L, "eth0"), Map.of(1L, "100"), Map.of(1L, "200"));

        dumper().dumpResults(List.of(result));

        List<Path> files = Files.list(tempDir).toList();
        Assert.assertEquals(1, files.size());

        String content = Files.readString(files.getFirst());
        String[] parts = content.trim().split("\\|");
        Assert.assertEquals("10.0.0.1", parts[0]);
        Assert.assertEquals("eth0", parts[1]);
        Assert.assertEquals("100", parts[2]);
        Assert.assertEquals("200", parts[3]);
    }

    @Test
    public void dumpResults_multipleHostsMultipleInterfaces() throws Exception {
        HostResult h1 = makeResult("10.0.0.1", 1000,
                Map.of(1L, "eth0", 2L, "eth1"),
                Map.of(1L, "1", 2L, "2"),
                Map.of(1L, "10", 2L, "20"));
        HostResult h2 = makeResult("10.0.0.2", 2000,
                Map.of(1L, "wlan0"),
                Map.of(1L, "5"),
                Map.of(1L, "50"));

        dumper().dumpResults(List.of(h1, h2));

        List<Path> files = Files.list(tempDir).toList();
        Assert.assertEquals(1, files.size());

        List<String> lines = Files.readAllLines(files.getFirst());
        Assert.assertEquals(3, lines.size());

        Assert.assertTrue(lines.stream().anyMatch(l -> l.startsWith("10.0.0.1|")));
        Assert.assertTrue(lines.stream().anyMatch(l -> l.startsWith("10.0.0.2|")));
    }

    @Test
    public void dumpResults_emptyCollection_createsEmptyFile() throws Exception {
        dumper().dumpResults(List.of());

        List<Path> files = Files.list(tempDir).toList();
        Assert.assertEquals(1, files.size());
        Assert.assertEquals(0, Files.readString(files.getFirst()).trim().length());
    }

    @Test
    public void dumpResults_existingFile_doesNotOverwrite() throws Exception {
        Path existing = tempDir.resolve("dump-999");
        Files.writeString(existing, "original");

        HostResult result = makeResult("10.0.0.1", 1000,
                Map.of(1L, "eth0"), Map.of(1L, "1"), Map.of(1L, "10"));

        dumper().dumpResults(List.of(result));

        Assert.assertEquals("original", Files.readString(existing));
    }

    @Test
    public void dumpResults_nullDumpDir_usesTempDir() throws Exception {
        Dumper dumper = new Dumper(null);
        HostResult result = makeResult("10.0.0.1", 1000,
                Map.of(1L, "eth0"), Map.of(1L, "1"), Map.of(1L, "10"));

        dumper.dumpResults(List.of(result));
        // no exception — file written somewhere under /tmp
    }

    // --- helpers ---

    private static HostResult makeResult(String host, long uptime,
                                          Map<Long, String> ifaces,
                                          Map<Long, String> inputs,
                                          Map<Long, String> outputs) {
        HostResult result = new HostResult(host);
        result.setUptime(uptime);
        result.setInterfaces(ifaces);
        result.setInputs(inputs);
        result.setOutputs(outputs);
        return result;
    }
}
