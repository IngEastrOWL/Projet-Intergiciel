package go.cs;

import go.Direction;
import go.Observer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class Channel<T> implements go.Channel<T> {

    private String name;
    private RemoteChannel channel; // stub fourni par RMI

    // liste locale pour garder une référence sur les observateurs distants
    private final List<RemoteObserver> observers = new ArrayList<>();

    public Channel(String name) {
        this.name = name;
        try {
            // connexion à l'annuaire RMI et récupération de la factory
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            RemoteChannelFactory server = (RemoteChannelFactory) registry.lookup("ChannelFactory");

            // Récupère le stub de notre canal auprès du serveur
            this.channel = server.getChannel(this.name);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to RMI Server", e);
        }
    }

    @Override
    public void out(T v) {
        try {
            channel.out(v); // délègue l'écriture au serveur RMI
        } catch (Exception e) {
            throw new RuntimeException("out error");
        }
    }

    @Override
    public T in() {
        try {
            return (T) channel.in(); // Délègue la lecture au serveur RMI
        } catch (Exception e) {
            throw new RuntimeException("in error");
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void observe(Direction direction, Observer observer) {
        try {
            // On transforme l'observer local en objet RMI (Remote)
            // pour que le serveur puisse appeler la méthode update() à distance (Callback)
            RemoteObserverImpl remoteObserver = new RemoteObserverImpl(observer);

            synchronized (observers) {
                observers.add(remoteObserver);
            }

            // enregistre de l'écouteur du client auprès du serveur RMI
            channel.observe(direction, remoteObserver);
        } catch (Exception e) {
            throw new RuntimeException("observe error");
        }
    }
}