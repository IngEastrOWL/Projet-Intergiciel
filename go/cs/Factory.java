package go.cs;

import go.Direction;
import go.Selector;
import go.shm.Channel;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Factory implements go.Factory {

    /** Création ou accès à un canal existant.
     * Côté serveur, le canal est créé au premier appel avec un nom donné ;
     * les appels suivants avec le même nom donneront accès au même canal.
     */
    public <T> go.Channel<T> newChannel(String name) {
        // TODO
        return new go.cs.Channel<T>(name);
    }
    
    /** Spécifie quels sont les canaux écoutés et la direction pour chacun. */
    @Override
    public Selector newSelector(Map<go.Channel, Direction> channels) {
        return new go.cs.Selector(channels);
    }

    /** Spécifie quels sont les canaux écoutés et la même di    rection pour tous. */
    public Selector newSelector(Set<go.Channel> channels, Direction direction) {
        return newSelector(channels
                           .stream() 
                           .collect(Collectors.toMap(Function.identity(), e -> direction)));
    }

}

