package net.paguo.statistics.snmp.database;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DBProxyFactoryTest {

    private Path tempFile;

    @Before
    public void setUp() throws IOException {
        tempFile = Files.createTempFile("test-dbprops", ".properties");
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    public void loadDefaultProperties_containsAllKeys() {
        Properties props = DBProxyFactory.loadDefaultProperties();

        Assert.assertTrue("has dbhost", props.containsKey(DBProxyFactory.HOST_KEY));
        Assert.assertTrue("has database", props.containsKey(DBProxyFactory.DATABASE_KEY));
        Assert.assertTrue("has username", props.containsKey(DBProxyFactory.USER_KEY));
        Assert.assertTrue("has password", props.containsKey(DBProxyFactory.PASSWORD_KEY));
    }

    @Test
    public void loadDefaultProperties_correctDefaultValues() {
        Properties props = DBProxyFactory.loadDefaultProperties();

        Assert.assertEquals("localhost", props.getProperty(DBProxyFactory.HOST_KEY));
        Assert.assertEquals("network_metrics", props.getProperty(DBProxyFactory.DATABASE_KEY));
        Assert.assertEquals("monitor_user", props.getProperty(DBProxyFactory.USER_KEY));
        Assert.assertEquals("monitor_password", props.getProperty(DBProxyFactory.PASSWORD_KEY));
    }

    @Test
    public void loadFileProperties_readsTempFile() throws IOException {
        Files.writeString(tempFile,
                "dbhost=prod-host\n" +
                "database=prod_db\n" +
                "username=admin\n" +
                "password=secret\n");

        Properties props = DBProxyFactory.loadFileProperties(tempFile.toString());

        Assert.assertEquals("prod-host", props.getProperty("dbhost"));
        Assert.assertEquals("prod_db", props.getProperty("database"));
        Assert.assertEquals("admin", props.getProperty("username"));
        Assert.assertEquals("secret", props.getProperty("password"));
    }

    @Test
    public void loadFileProperties_readsSubsetOfKeys() throws IOException {
        Files.writeString(tempFile, "dbhost=override-host\n");

        Properties props = DBProxyFactory.loadFileProperties(tempFile.toString());

        Assert.assertEquals("override-host", props.getProperty("dbhost"));
        Assert.assertNull("no database key", props.getProperty("database"));
    }

    @Test(expected = IOException.class)
    public void loadFileProperties_nonExistentFile_throws() throws IOException {
        DBProxyFactory.loadFileProperties("/nonexistent/path/to/file.properties");
    }

    @Test
    public void getDBProxy_returnsSameInstance() {
        DBProxy first = DBProxyFactory.getDBProxy();
        DBProxy second = DBProxyFactory.getDBProxy();
        Assert.assertSame(first, second);
    }

    @Test
    public void getDBProxy_returnsDBProxyImpl() {
        DBProxy proxy = DBProxyFactory.getDBProxy();
        Assert.assertTrue(proxy instanceof DBProxyImpl);
    }

    @Test
    public void keyConstants_matchExpectedNames() {
        Assert.assertEquals("dbhost", DBProxyFactory.HOST_KEY);
        Assert.assertEquals("database", DBProxyFactory.DATABASE_KEY);
        Assert.assertEquals("username", DBProxyFactory.USER_KEY);
        Assert.assertEquals("password", DBProxyFactory.PASSWORD_KEY);
    }
}
