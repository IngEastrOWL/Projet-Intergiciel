package go.sock;

import go.Channel;
import go.Direction;
import go.Selector;

import java.util.Map;
import java.util.Set;

public class Factory implements go.Factory {

    // Factory interne pour la gestion de la mémoire partagée
    private final go.shm.Factory shmFactory = new go.shm.Factory();

    @Override
    public <T> Channel<T> newChannel(String name) {
        try {
            // demande au service de nommage pour vérifier si le canal existe déjà
            String address = Naming.lookup(name);

            if (address == null) {
                // Si l'adresse n'existe pas, ce processus est le premier : il devient master
                return new ChannelMaster<T>(name, shmFactory);
            } else {
                // Si l'adresse existe, on extrait l'hôte et le port pour se connecter au master
                String[] parts = address.split(":");
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);

                // Le processus devient slave
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
