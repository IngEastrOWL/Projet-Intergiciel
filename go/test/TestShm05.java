package go.test;

import go.Direction;
import go.*;
import java.util.Set;

public class TestShm05 {

    private static void quit(String msg) {
        System.out.println("TestShm05: " + msg);
        System.exit(msg.equals("ok") ? 0 : 1);
    }

    public static void main(String[] a) {
        Factory factory = new go.shm.Factory();
        Channel<Integer> c1 = factory.newChannel("c1");
        Channel<Integer> c2 = factory.newChannel("c2");

        Selector s = factory.newSelector(Set.of(c1, c2), Direction.Out);

        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
            quit("KO (deadlock)");
        }).start();

        // only c1 has a receiver waiting
        new Thread(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            int v = c1.in();
            if (v != 55) quit("KO: expected 55, got " + v);
        }).start();

        new Thread(() -> {
            try { Thread.sleep(300); } catch (InterruptedException e) {}
            @SuppressWarnings("unchecked")
            Channel<Integer> selected = s.select();
            if (selected != c1) quit("KO: wrong channel selected, expected c1");
            selected.out(55);
            quit("ok");
        }).start();
    }
}