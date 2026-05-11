package go.test;

import go.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TestShm06 {

    private static void quit(String msg) {
        System.out.println("TestShm06: " + msg);
        System.exit(msg.equals("ok") ? 0 : 1);
    }

    public static void main(String[] a) throws InterruptedException {
        Factory factory = new go.shm.Factory();
        Channel<Integer> c = factory.newChannel("c");
        int N = 5;

        new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            quit("KO (deadlock)");
        }).start();

        ConcurrentHashMap<Integer, Integer> received = new ConcurrentHashMap<>();
        AtomicInteger done = new AtomicInteger(0);

        for (int i = 0; i < N; i++) {
            new Thread(() -> {
                int v = c.in();
                received.merge(v, 1, Integer::sum);
                if (done.incrementAndGet() == N) {
                    for (int j = 0; j < N; j++) {
                        if (!received.containsKey(j)) quit("KO: missing value " + j);
                        if (received.get(j) != 1) quit("KO: duplicate for value " + j);
                    }
                    quit("ok");
                }
            }).start();
        }

        new Thread(() -> {
            for (int i = 0; i < N; i++) {
                final int val = i;
                new Thread(() -> c.out(val)).start();
            }
        }).start();
    }
}