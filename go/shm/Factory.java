package go.shm;


import go.Direction;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Factory implements go.Factory {

    /** Création ou accès à un canal existant. */
    public <T> go.Channel<T> newChannel(String name) {
        // TODO : Proposition
        return new Channel<T>(name);
    }

    /** Spécifie quels sont les canaux écoutés et la direction pour chacun. */
    public go.Selector newSelector(Map<go.Channel, Direction> channels) {
        return new go.shm.Selector(channels);
    }

    /** Spécifie quels sont les canaux écoutés et la même direction pour tous. */
    public go.Selector newSelector(Set<go.Channel> channels, Direction direction) {
        // TODO : Proposition

        /* Ici on récupère les channels dans un stream et chaque élément du set devient une clé (Function.identity) du map et
         chaque clés associé a la même valeur. (c -> direction) */
        Map<go.Channel, Direction> map =
                channels.stream().collect(Collectors.toMap(Function.identity(), c -> direction));
        return new Selector(map);
    }

}
