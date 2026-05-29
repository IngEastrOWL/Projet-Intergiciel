package go.cs;

import go.Channel;
import go.Direction;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implantation d'un serveur hébergeant des canaux.
 *
 */
public class ServerImpl extends UnicastRemoteObject implements RemoteChannelFactory {
    private final Map<String, RemoteChannel> channels = new HashMap<>();
    private final go.shm.Factory shmFactory = new go.shm.Factory();
    private final Map<Map<String, Direction>, go.Selector> selectors = new HashMap<>();

    protected ServerImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized RemoteChannel getChannel(String name) throws RemoteException {
        if (channels.containsKey(name)) {
            return channels.get(name);
        } else {
            RemoteChannel channel = new RemoteChannelImpl<Objects>(name, shmFactory);
            channels.put(name, channel);
            return channel;
        }
    }

    @Override
    public String select(Map<String, Direction> watched) throws RemoteException {
        go.Selector selec;

        synchronized (this) {
            if (selectors.containsKey(watched)) {
                selec = selectors.get(watched);
            } else {
                Map<go.Channel, Direction> map = new HashMap<>();
                for (Map.Entry<String, Direction> e : watched.entrySet()) {
                    String name = e.getKey();
                    if (!channels.containsKey(name)) {
                        getChannel(name);
                    }
                    RemoteChannelImpl<?> channel = (RemoteChannelImpl<?>) channels.get(name);
                    map.put(channel.getChannel(), e.getValue());
                }
                selec = shmFactory.newSelector(map);
                selectors.put(watched, selec);
            }
        }
        go.Channel c = selec.select();
        return c.getName();
    }


    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            ServerImpl factory = new ServerImpl();
            registry.rebind("ChannelFactory", factory);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
