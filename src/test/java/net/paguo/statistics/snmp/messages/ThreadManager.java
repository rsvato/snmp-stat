package net.paguo.statistics.snmp.messages;

import java.util.concurrent.*;
import java.util.Random;
import java.util.List;
import java.util.LinkedList;

/**
 * User: sreentenko
 * Date: 31.01.2009
 * Time: 0:50:52
 */
public class ThreadManager {
    public static void main(String args[]) throws InterruptedException {
        ExecutorService pool = Executors.newCachedThreadPool();
        final java.util.concurrent.ConcurrentMap<Integer, Integer> map = new ConcurrentHashMap<Integer, Integer>();
        final List<Future<String>> futures = new LinkedList<Future<String>>();
        for (int i = 0; i < 200; i++){
            final int i2 = i;
            final Random r = new Random();
            Future<String> future = pool.submit(new Callable<String>() {
                public String call() throws Exception {
                    int sleepTime = r.nextInt(5000);
                    Thread.sleep(sleepTime);
                    map.put(i2, sleepTime);
                    System.out.println("FINISHED  " + i2);
                    return "Ok";
                }
            });
            futures.add(future);
        }
        System.out.println("Stopping execution");
        pool.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            do {
                System.out.println("Awaiting termination");
                Thread.sleep(600);
            } while(map.size() < 200);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Map size: " + map.size());
        int i = 0;
        System.out.println("===== FUTURES: =====");
        for (Future<String> future : futures) {
            if (future.isDone()){
                System.out.println(i++);
            }else{
                System.out.println("Waiting termination");
                Thread.sleep(2000);
            }
        }
    }
}
