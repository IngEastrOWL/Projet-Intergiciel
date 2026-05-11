package go.test;

import go.Direction;
import go.*;
import java.util.Set;

public class TestShm04 {

    private static void quit(String msg) {
        System.out.println("TestShm04: " + msg);
        System.exit(msg.equals("ok") ? 0 : 1);
    }

    public static void main(String[] a) {
        Factory factory = new go.shm.Factory();
        Channel<Integer> c1 = factory.newChannel("c1");
        Channel<Integer> c2 = factory.newChannel("c2");

        Selector s = factory.newSelector(Set.of(c1, c2), Direction.In);

        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
            quit("KO (deadlock)");
        }).start();

        // only c2 sends
        new Thread(() -> {
            try { Thread.sleep(200); } catch (InterruptedException e) {}
            c2.out(77);
        }).start();

        new Thread(() -> {
            @SuppressWarnings("unchecked")
            Channel<Integer> selected = s.select();
            if (selected != c2) quit("KO: wrong channel selected");
            int v = selected.in();
            if (v != 77) quit("KO: expected 77, got " + v);
            quit("ok");
        }).start();
    }
}