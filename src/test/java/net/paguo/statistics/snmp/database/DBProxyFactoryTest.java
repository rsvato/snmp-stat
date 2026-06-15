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

        Assert.assertTrue("has dbUrl", props.containsKey(DBProxyFactory.DB_URL_KEY));
        Assert.assertTrue("has username", props.containsKey(DBProxyFactory.USER_KEY));
        Assert.assertTrue("has password", props.containsKey(DBProxyFactory.PASSWORD_KEY));
    }

    @Test
    public void loadDefaultProperties_correctDefaultValues() {
        Properties props = DBProxyFactory.loadDefaultProperties();

        Assert.assertEquals("jdbc:postgresql://localhost:5432/network_metrics", props.getProperty(DBProxyFactory.DB_URL_KEY));
        Assert.assertEquals("monitor_user", props.getProperty(DBProxyFactory.USER_KEY));
        Assert.assertEquals("monitor_password", props.getProperty(DBProxyFactory.PASSWORD_KEY));
    }

    @Test
    public void loadFileProperties_readsTempFile() throws IOException {
        Files.writeString(tempFile,
                "dbUrl=jdbc:postgresql://prod:5432/prod_db\n" +
                "username=admin\n" +
                "password=secret\n");

        Properties props = DBProxyFactory.loadFileProperties(tempFile.toString());

        Assert.assertEquals("jdbc:postgresql://prod:5432/prod_db", props.getProperty("dbUrl"));
        Assert.assertEquals("admin", props.getProperty("username"));
        Assert.assertEquals("secret", props.getProperty("password"));
    }

    @Test
    public void loadFileProperties_readsSubsetOfKeys() throws IOException {
        Files.writeString(tempFile, "dbUrl=jdbc:postgresql://other:5432/db\n");

        Properties props = DBProxyFactory.loadFileProperties(tempFile.toString());

        Assert.assertEquals("jdbc:postgresql://other:5432/db", props.getProperty("dbUrl"));
        Assert.assertNull("no username key", props.getProperty("username"));
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
        Assert.assertEquals("username", DBProxyFactory.USER_KEY);
        Assert.assertEquals("password", DBProxyFactory.PASSWORD_KEY);
        Assert.assertEquals("dbUrl", DBProxyFactory.DB_URL_KEY);
    }
}
