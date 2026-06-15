package net.paguo.statistics.snmp.commands;

import org.junit.Assert;
import org.junit.Test;
import org.snmp4j.smi.OID;

public class SnmpSessionChildOidTest {

    @Test
    public void testChild_directDescendant() {
        OID base = new OID(".1.3.6.1.2.1.2.2.1.2");
        OID child = new OID(".1.3.6.1.2.1.2.2.1.2.5");
        Assert.assertTrue(new SnmpSession().child(base, child));
    }

    @Test
    public void testChild_sameOid() {
        OID base = new OID(".1.3.6.1.2.1.2.2.1.2");
        Assert.assertTrue(new SnmpSession().child(base, base));
    }

    @Test
    public void testChild_siblingOid() {
        OID base = new OID(".1.3.6.1.2.1.2.2.1.2");
        OID sibling = new OID(".1.3.6.1.2.1.2.2.1.10");
        Assert.assertFalse(new SnmpSession().child(base, sibling));
    }

    @Test
    public void testChild_differentSubtree() {
        OID base = new OID(".1.3.6.1.2.1.1.3.0");
        OID other = new OID(".1.3.6.1.2.1.2.2.1.2");
        Assert.assertFalse(new SnmpSession().child(base, other));
    }

    @Test
    public void testChild_partialPrefixMatch() {
        // String-based prefix: ".1.3.6.1.2.1.2.2.1.1" is a prefix of ".1.3.6.1.2.1.2.2.1.10"
        OID base = new OID(".1.3.6.1.2.1.2.2.1.1");
        OID oid = new OID(".1.3.6.1.2.1.2.2.1.10");
        Assert.assertTrue(new SnmpSession().child(base, oid));
    }

    @Test
    public void testChild_shorterOidThanBase() {
        OID base = new OID(".1.3.6.1.2.1.2.2.1.16");
        OID shorter = new OID(".1.3.6.1.2.1");
        Assert.assertFalse(new SnmpSession().child(base, shorter));
    }
}
