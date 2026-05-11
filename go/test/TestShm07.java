package go.test;

import go.Direction;
import go.*;
import java.util.Map;

public class TestShm07 {

    private static void quit(String msg) {
        System.out.println("TestShm07: " + msg);
        System.exit(msg.equals("ok") ? 0 : 1);
    }

    public static void main(String[] a) {
        Factory factory = new go.shm.Factory();
        Channel<Integer> c1 = factory.newChannel("c1");
        Channel<Integer> c2 = factory.newChannel("c2");

        Selector s = factory.newSelector(Map.of(c1, Direction.In, c2, Direction.Out));

        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
            quit("KO (deadlock)");
        }).start();

        new Thread(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            int v = c2.in();
            if (v != 999) quit("KO: receiver on c2 got " + v);
        }).start();

        new Thread(() -> {
            try { Thread.sleep(300); } catch (InterruptedException e) {}
            @SuppressWarnings("unchecked")
            Channel<Integer> selected = s.select();
            if (selected != c2) quit("KO: expected c2 (Out-ready), got " + selected.getName());
            selected.out(999);
            quit("ok");
        }).start();
    }
}