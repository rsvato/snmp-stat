package net.paguo.statistics.snmp.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * User: slava
 * Date: 26.04.2007
 * Time: 0:22:17
 * Version: $Id$
 */
public class DBProxyFactory {
    private static final Logger log = LoggerFactory.getLogger(DBProxyFactory.class);
    private static DBProxy proxy;
    static final String HOST_KEY        = "dbhost";
    static final String DATABASE_KEY    = "database";
    static final String USER_KEY        = "username";
    static final String PASSWORD_KEY    = "password";

    public static DBProxy getDBProxy() {
        if (proxy == null) {
            Properties props = loadProperties();
            proxy = new DBProxy(props);
        }
        return proxy;
    }

    private static Properties loadProperties() {
        Properties props = loadDefaultProperties();
        String propertyFile = System.getProperty("dbprops");
        if (propertyFile != null) {
            try {
                props = loadFileProperties(propertyFile);
            } catch (IOException e) {
                log.error("Error loading properties", e);
            }
        }
        return props;
    }

    private static Properties loadFileProperties(String propertyFile) throws IOException {
        Properties props;
        try (InputStream is = Files.newInputStream(Paths.get(propertyFile))) {
            props = new Properties();
            props.load(is);
        }
        return props;
    }

    private static Properties loadDefaultProperties() {
        Properties props = new Properties();
        try (InputStream defaultSource = DBProxyFactory.class.getClassLoader().getResourceAsStream("default.properties")) {
            props.load(defaultSource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return props;
    }

    public static DBProxy getDBProxy(Properties props) {
        if (proxy == null) {
            proxy = new DBProxy(props);
        }
        return proxy;
    }
}
