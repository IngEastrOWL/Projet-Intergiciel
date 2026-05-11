package go.shm;

import go.Channel;
import go.Direction;
import go.Observer;

import java.util.Map;

public class Selector implements go.Selector {

    private final Map<Channel, Direction> channels;
    private Channel selected = null;

    public Selector(Map<Channel, Direction> channels) {
        this.channels = channels;
    }

    @Override
    public Channel select() {
        synchronized (this) {
            selected = null;

            while (selected == null) {
                for (Map.Entry<Channel, Direction> e : channels.entrySet()) {
                    go.shm.Channel<?> ch = (go.shm.Channel<?>) e.getKey();
                    if (ch.isReady(e.getValue())) {
                        return e.getKey();
                    }
                }

                Observer obs = () -> {
                    synchronized (Selector.this) {
                        if (selected != null) return;

                        for (Map.Entry<Channel, Direction> e : channels.entrySet()) {
                            go.shm.Channel<?> ch = (go.shm.Channel<?>) e.getKey();
                            if (ch.isReady(e.getValue())) {
                                selected = e.getKey();
                                Selector.this.notifyAll();
                                return;
                            }
                        }
                    }
                };

                for (Map.Entry<Channel, Direction> e : channels.entrySet()) {
                    e.getKey().observe(Direction.inverse(e.getValue()), obs);
                }

                for (Map.Entry<Channel, Direction> e : channels.entrySet()) {
                    go.shm.Channel<?> ch = (go.shm.Channel<?>) e.getKey();
                    if (ch.isReady(e.getValue())) {
                        return e.getKey();
                    }
                }

                try {
                    while (selected == null) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }

            return selected;
        }
    }
}