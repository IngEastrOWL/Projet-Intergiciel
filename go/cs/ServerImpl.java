package go.cs;

import go.Channel;
import go.Direction;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Implantation d'un serveur hébergeant des canaux.
 *
 */
public class ServerImpl extends UnicastRemoteObject implements RemoteChannelFactory {
    private final Map<String, RemoteChannel> channels = new HashMap<>();
    private final go.shm.Factory shmFactory = new go.shm.Factory();


    protected ServerImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized RemoteChannel getChannel(String name) throws RemoteException {
        if (channels.containsKey(name)) {
            return channels.get(name);
        } else {
            RemoteChannel channel = new RemoteChannelImpl<>(name, shmFactory);
            channels.put(name, channel);
            return channel;
        }
    }

    @Override
    public synchronized String select(Map<String, Direction> watched) throws RemoteException {
        Map<go.Channel, Direction> map = new HashMap<>();
        for (Map.Entry<String, Direction> e : watched.entrySet()) {
            map.put(shmFactory.newChannel(e.getKey()), e.getValue());
        }
        go.Selector s = shmFactory.newSelector(map);
        go.Channel c = s.select();
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
