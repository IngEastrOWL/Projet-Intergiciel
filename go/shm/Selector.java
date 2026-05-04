package go.shm;

import go.Direction;
import go.Channel;
import go.Observer;

import java.util.Map;
import java.util.Set;

public class Selector implements go.Selector {

    private Map<Channel, Direction> channels;
    private Channel selectedChannel = null;

    public Selector(Map<Channel, Direction> channels) {
        this.channels = channels;
    }

    public Channel select() {
        synchronized (this) {
            for (Map.Entry<Channel, Direction> entry : channels.entrySet()) {
                entry.getKey().observe(Direction.inverse(entry.getValue()), new Observer() {
                    @Override
                    public void update() {
                        synchronized (Selector.this) {
                            if (selectedChannel == null) {
                                selectedChannel = entry.getKey();
                                Selector.this.notify();
                            }
                        }
                    }
                });
                try {
                    while (selectedChannel == null) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return selectedChannel;
        }
    }

}
