package go.sock;

import go.Channel;
import go.Direction;
import go.Selector;

import java.util.Map;
import java.util.Set;

public class Factory implements go.Factory {

    private final go.shm.Factory shmFactory = new go.shm.Factory();

    @Override
    public <T> Channel<T> newChannel(String name) {
        try {
            String address = Naming.lookup(name);
            if (address == null) {
                return new ChannelMaster<T>(name, shmFactory);
            } else {
                String[] parts = address.split(":");
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                return new ChannelSlave<T>(name, host, port);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Selector newSelector(Map<Channel, Direction> channels) {
        return null;
    }

    @Override
    public Selector newSelector(Set<Channel> channels, Direction direction) {
        return null;
    }
}
