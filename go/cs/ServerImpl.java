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

    protected ServerImpl() throws RemoteException {
    }

    @Override
    public RemoteChannel getChannel(String name) throws RemoteException {
        if (channels.containsKey(name)) {
            return channels.get(name);
        } else {
            RemoteChannel channel = new RemoteChannelImpl(name);
            channels.put(name, channel);
            return channel;
        }
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
