package go.cs;

import go.Channel;
import go.Direction;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class Selector implements go.Selector {

    private final Map<Channel, Direction> channels;

    public Selector(Map<Channel, Direction> channels) {
        this.channels = channels;
    }

    @Override
    public Channel select() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            RemoteChannelFactory server =
                    (RemoteChannelFactory) registry.lookup("ChannelFactory");

            Map<String, Direction> watched = new HashMap<>();
            for (Map.Entry<Channel, Direction> e : channels.entrySet()) {
                watched.put(e.getKey().getName(), e.getValue());
            }

            String selectedName = server.select(watched);

            for (Channel c : channels.keySet()) {
                if (c.getName().equals(selectedName)) {
                    return c;
                }
            }

            throw new RuntimeException("Selected channel introuvable: " + selectedName);

        } catch (Exception e) {
            throw new RuntimeException("select error", e);
        }
    }
}