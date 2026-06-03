package go.shm;

import go.Direction;
import go.Observer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Channel<T> implements go.Channel<T> {

    private final String name;

    private static class InRequest {
        boolean matched = false;
        Object value = null;
    }

    private static class OutRequest {
        boolean matched = false;
        boolean consumed = false;
        Object value;

        OutRequest(Object value) {
            this.value = value;
        }
    }

    private final Deque<InRequest> waitingIns = new ArrayDeque<>();
    private final Deque<OutRequest> waitingOuts = new ArrayDeque<>();

    private final List<Observer> inObservers = new ArrayList<>();
    private final List<Observer> outObservers = new ArrayList<>();

    public Channel(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T in() {
        InRequest inReq;
        OutRequest outReq = null;
        List<Observer> declencheur = null;

        synchronized (this) {
            // si un écrivain attend déjà, on fait le rendez-vous immédiatement
            if (!waitingOuts.isEmpty()) {
                outReq = waitingOuts.removeFirst();
                outReq.matched = true;
                T v = (T) outReq.value;
                outReq.consumed = true;
                notifyAll(); // Réveille l'écrivain bloqué
                return v;
            }

            // sinon, on enregistre dans la file des lecteurs en attente
            inReq = new InRequest();
            waitingIns.addLast(inReq);

            if (!inObservers.isEmpty()) {
                declencheur = new ArrayList<>(inObservers);
                inObservers.clear();
            }

            notifyAll();
        }

        if (declencheur != null) {
            for (Observer o : declencheur) {
                try { o.update(); } catch (Throwable ignored) {}
            }
        }

        // blocage du thread jusqu'à ce qu'un écrivain donne une valeur
        synchronized (this) {
            try {
                while (!inReq.matched) {
                    wait();
                }
                return (T) inReq.value;

            } catch (InterruptedException e) {
                waitingIns.remove(inReq);
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void out(T v) {
        OutRequest outReq;
        List<Observer> declencheur = null;

        synchronized (this) {
            // si un lecteur attend on lui donne la valeur directement
            if (!waitingIns.isEmpty()) {
                InRequest inReq = waitingIns.removeFirst();
                inReq.matched = true;
                inReq.value = v;
                notifyAll(); // réveille le lecteur bloqué
                return;
            }

            // Sinon on stocke notre requête d'écriture dans la file
            outReq = new OutRequest(v);
            waitingOuts.addLast(outReq);

            if (!outObservers.isEmpty()) {
                declencheur = new ArrayList<>(outObservers);
                outObservers.clear();
            }

            notifyAll();
        }

        if (declencheur != null) {
            for (Observer o : declencheur) {
                try { o.update(); } catch (Throwable ignored) {}
            }
        }

        // blocage du thread jusqu'à ce qu'un lecteur consomme la donnée
        synchronized (this) {
            try {
                while (!outReq.consumed) {
                    wait();
                }
            } catch (InterruptedException e) {
                waitingOuts.remove(outReq);
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void observe(Direction direction, Observer observer) {
        boolean immediate;

        synchronized (this) {
            if (direction == Direction.In) {
                // Si quelqu'un attend pour lire, le canal est disponible
                immediate = !waitingIns.isEmpty();
                if (!immediate) {
                    inObservers.add(observer);
                }
            } else {
                // Si quelqu'un attend pour écrire, le canal est disponible
                immediate = !waitingOuts.isEmpty();
                if (!immediate) {
                    outObservers.add(observer);
                }
            }
        }

        // si la condition était déjà remplie, on déclenche l'observer tout de suite
        if (immediate) {
            observer.update();
        }
    }

    // Indique si une opération IN ou OUT peut se faire sans bloquer
    public synchronized boolean isReady(Direction dir) {
        if (dir == Direction.In) {
            return !waitingOuts.isEmpty(); // pret à lire si un écrivain attend
        } else {
            return !waitingIns.isEmpty();  // pret à écrire si un lecteur attend
        }
    }
}