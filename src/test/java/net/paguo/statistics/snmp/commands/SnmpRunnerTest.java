package net.paguo.statistics.snmp.commands;

import net.paguo.statistics.snmp.model.HostDefinition;
import net.paguo.statistics.snmp.model.HostResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class SnmpRunnerTest {

    @Mock
    private SnmpHostProcessor processor;

    private SnmpRunner runner;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        runner = new SnmpRunner(processor);
    }

    @Test
    public void queryHostDefinitions_filtersInvalidResults() {
        HostResult valid = makeValidResult("10.0.0.1");
        HostResult invalid = new HostResult("10.0.0.2"); // all null

        when(processor.call(any(HostDefinition.class)))
                .thenReturn(valid)
                .thenReturn(invalid);

        Set<HostDefinition> definitions = Set.of(
                new HostDefinition("10.0.0.1", "public"),
                new HostDefinition("10.0.0.2", "public")
        );

        List<HostResult> results = runner.queryHostDefinitions(definitions);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals("10.0.0.1", results.getFirst().getHostAddress());
    }

    @Test
    public void queryHostDefinitions_nullFromProcessor_isFiltered() {
        when(processor.call(any(HostDefinition.class)))
                .thenReturn(null)
                .thenReturn(makeValidResult("10.0.0.2"));

        Set<HostDefinition> definitions = Set.of(
                new HostDefinition("10.0.0.1", "public"),
                new HostDefinition("10.0.0.2", "public")
        );

        List<HostResult> results = runner.queryHostDefinitions(definitions);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals("10.0.0.2", results.getFirst().getHostAddress());
    }

    @Test
    public void queryHostDefinitions_emptyInput_returnsEmpty() {
        List<HostResult> results = runner.queryHostDefinitions(Set.of());
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    public void queryHostDefinitions_allInvalid_returnsEmpty() {
        when(processor.call(any(HostDefinition.class))).thenReturn(null);

        Set<HostDefinition> definitions = Set.of(
                new HostDefinition("10.0.0.1", "public"),
                new HostDefinition("10.0.0.2", "public")
        );

        List<HostResult> results = runner.queryHostDefinitions(definitions);
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    public void queryHostDefinitions_allValid_returnsAll() {
        HostResult r1 = makeValidResult("10.0.0.1");
        HostResult r2 = makeValidResult("10.0.0.2");
        HostResult r3 = makeValidResult("10.0.0.3");

        when(processor.call(any(HostDefinition.class)))
                .thenReturn(r1).thenReturn(r2).thenReturn(r3);

        Set<HostDefinition> definitions = Set.of(
                new HostDefinition("10.0.0.1", "public"),
                new HostDefinition("10.0.0.2", "public"),
                new HostDefinition("10.0.0.3", "public")
        );

        List<HostResult> results = runner.queryHostDefinitions(definitions);

        Assert.assertEquals(3, results.size());
    }

    // --- helpers ---

    private static HostResult makeValidResult(String host) {
        HostResult result = new HostResult(host);
        result.setUptime(1000);
        result.setInterfaces(Map.of(1L, "eth0"));
        result.setInputs(Map.of(1L, "100"));
        result.setOutputs(Map.of(1L, "200"));
        return result;
    }
}
