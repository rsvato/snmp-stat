package net.paguo.statistics.snmp;

import net.paguo.statistics.snmp.model.HostQuery;
import net.paguo.statistics.snmp.model.HostDefinition;
import net.paguo.statistics.snmp.commands.HostRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Set;

/**
 * User: slava
 * Date: 25.04.2007
 * Time: 23:45:11
 * Version: $Id$
 */
@Deprecated
public class MainOld {
    public static final Logger log = LoggerFactory.getLogger(MainOld.class);

    public static void main(String args[]) throws SQLException, InterruptedException {
        Thread t = new Thread(new Runner());
        t.start();
        t.join();
    }

    public static class Runner implements Runnable{

        public void run() {
            log.debug("Starting main thread. Getting hosts");
            long start = System.currentTimeMillis();
            HostQuery q = new HostQuery();
            Set<HostDefinition> hostDefinitions = q.getDefinitions();
            for (HostDefinition hd : hostDefinitions) {
                log.debug(hd.getHostAddress());
                Runnable runner = new HostRunner(hd);
                Thread t = new Thread(runner);
                t.start();
            }

            long runTime = System.currentTimeMillis() - start;
            log.debug("All threads finished. Total time " + runTime + " ms");
        }
    }
}
