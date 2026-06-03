package go.shm;

import go.Channel;
import go.Direction;
import go.Observer;

import java.util.Map;

public class Selector implements go.Selector {

    private final Map<Channel, Direction> channels;
    private Channel selected = null;

    public Selector(Map<Channel, Direction> channels) {
        this.channels = channels;
    }

    @Override
    public Channel select() {
        synchronized (this) {
            selected = null;

            while (selected == null) {

                // si un canal est prêt au moment de l'appel, on le retourne directement
                for (Map.Entry<Channel, Direction> e : channels.entrySet()) {
                    go.shm.Channel<?> ch = (go.shm.Channel<?>) e.getKey();
                    if (ch.isReady(e.getValue())) {
                        return e.getKey();
                    }
                }

                // Ce bloc de code sera déclenché plus tard par un autre thread dès qu'un canal bouge
                Observer obs = () -> {
                    synchronized (Selector.this) {
                        if (selected != null) return;

                        // on cherche le canal débloqué pour le désigner comme "élu"
                        for (Map.Entry<Channel, Direction> e : channels.entrySet()) {
                            go.shm.Channel<?> ch = (go.shm.Channel<?>) e.getKey();
                            if (ch.isReady(e.getValue())) {
                                selected = e.getKey();
                                Selector.this.notifyAll(); // reveille le thread du Selector qui dort
                                return;
                            }
                        }
                    }
                };

                // abonnement aux événements de TOUS les canaux surveillés
                for (Map.Entry<Channel, Direction> e : channels.entrySet()) {
                    e.getKey().observe(Direction.inverse(e.getValue()), obs);
                }

                // Évite de s'endormir si un canal s'est activé PENDANT qu'on ajotuais les observateurs
                for (Map.Entry<Channel, Direction> e : channels.entrySet()) {
                    go.shm.Channel<?> ch = (go.shm.Channel<?>) e.getKey();
                    if (ch.isReady(e.getValue())) {
                        return e.getKey();
                    }
                }

                // aucun canal n'est pret, on relâche le verrou et on attend le signal de l'Observer
                try {
                    while (selected == null) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }

            return selected;
        }
    }
}