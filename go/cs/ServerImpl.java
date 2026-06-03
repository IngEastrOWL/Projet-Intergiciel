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

    // registres internes pour stocker les canaux et les sélecteurs actifs sur le serveur
    private final Map<String, RemoteChannel> channels = new HashMap<>();
    private final go.shm.Factory shmFactory = new go.shm.Factory();
    private final Map<Map<String, Direction>, go.Selector> selectors = new HashMap<>();

    protected ServerImpl() throws RemoteException {
        super();
    }

    // Récupère un canal existant ou le crée à la volée s'il n'existe pas encore
    @Override
    public synchronized RemoteChannel getChannel(String name) throws RemoteException {
        if (channels.containsKey(name)) {
            return channels.get(name);
        } else {
            // Le canal distant encapsule un canal en mémoire partagée locale (shm)
            RemoteChannel channel = new RemoteChannelImpl<Objects>(name, shmFactory);
            channels.put(name, channel);
            return channel;
        }
    }

    @Override
    public String select(Map<String, Direction> watched) throws RemoteException {
        go.Selector selec;

        synchronized (this) {
            // vérifie si un selector correspondant existe déjà
            if (selectors.containsKey(watched)) {
                selec = selectors.get(watched);
            } else {
                // traduction de la Map en Map d'objets Channel
                Map<go.Channel, Direction> map = new HashMap<>();
                for (Map.Entry<String, Direction> e : watched.entrySet()) {
                    String name = e.getKey();
                    if (!channels.containsKey(name)) {
                        getChannel(name);
                    }
                    RemoteChannelImpl<?> channel = (RemoteChannelImpl<?>) channels.get(name);
                    map.put(channel.getChannel(), e.getValue());
                }
                // création du selector local basé sur la mémoire partagée
                selec = shmFactory.newSelector(map);
                selectors.put(watched, selec);
            }
        }

        // appel bloquant côté serveur : le thread RMI attend qu'un canal local soit prêt
        go.Channel c = selec.select();
        return c.getName();
    }

    // initialisation de l'annuaire RMI et y enregistre la Factory
    public static void main(String[] args) {
        try {
            // creation du registre RMI sur le port standard 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            ServerImpl factory = new ServerImpl();

            // Publication de la factory  pour les clients
            registry.rebind("ChannelFactory", factory);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}