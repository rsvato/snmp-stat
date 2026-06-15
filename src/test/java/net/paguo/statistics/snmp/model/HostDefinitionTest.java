package net.paguo.statistics.snmp.model;

import org.junit.Assert;
import org.junit.Test;

public class HostDefinitionTest {

    @Test
    public void equals_sameValues() {
        HostDefinition a = new HostDefinition("10.0.0.1", "public");
        HostDefinition b = new HostDefinition("10.0.0.1", "public");
        Assert.assertEquals(a, b);
    }

    @Test
    public void equals_differentAddress() {
        HostDefinition a = new HostDefinition("10.0.0.1", "public");
        HostDefinition b = new HostDefinition("10.0.0.2", "public");
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void equals_differentCommunity() {
        HostDefinition a = new HostDefinition("10.0.0.1", "public");
        HostDefinition b = new HostDefinition("10.0.0.1", "private");
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void equals_nullIsNotEqual() {
        HostDefinition def = new HostDefinition("10.0.0.1", "public");
        Assert.assertNotEquals(null, def);
    }

    @Test
    public void equals_reflexive() {
        HostDefinition def = new HostDefinition("10.0.0.1", "public");
        Assert.assertEquals(def, def);
    }

    @Test
    public void hashCode_sameValues_sameHash() {
        HostDefinition a = new HostDefinition("10.0.0.1", "public");
        HostDefinition b = new HostDefinition("10.0.0.1", "public");
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void hashCode_differentValues_differentHash() {
        HostDefinition a = new HostDefinition("10.0.0.1", "public");
        HostDefinition b = new HostDefinition("10.0.0.2", "public");
        // Not guaranteed but extremely likely
        Assert.assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void toString_containsBothFields() {
        HostDefinition def = new HostDefinition("10.0.0.1", "public");
        String s = def.toString();
        Assert.assertTrue(s.contains("10.0.0.1"));
        Assert.assertTrue(s.contains("public"));
    }

    @Test
    public void accessors_returnCorrectValues() {
        HostDefinition def = new HostDefinition("10.0.0.1", "public");
        Assert.assertEquals("10.0.0.1", def.hostAddress());
        Assert.assertEquals("public", def.community());
    }

    @Test
    public void constructor_acceptsNullComponents() {
        HostDefinition def = new HostDefinition(null, null);
        Assert.assertNull(def.hostAddress());
        Assert.assertNull(def.community());
    }
}
