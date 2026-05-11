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
        List<Observer> toFire = null;

        synchronized (this) {
            if (!waitingOuts.isEmpty()) {
                outReq = waitingOuts.removeFirst();
                outReq.matched = true;
                T v = (T) outReq.value;
                outReq.consumed = true;
                notifyAll();
                return v;
            }

            inReq = new InRequest();
            waitingIns.addLast(inReq);

            if (!inObservers.isEmpty()) {
                toFire = new ArrayList<>(inObservers);
                inObservers.clear();
            }

            notifyAll();
        }

        if (toFire != null) {
            for (Observer o : toFire) {
                try {
                    o.update();
                } catch (Throwable ignored) {
                }
            }
        }

        synchronized (this) {
            try {
                while (!inReq.matched) {
                    wait();
                }

                T v = (T) inReq.value;
                return v;

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
        List<Observer> toFire = null;

        synchronized (this) {
            if (!waitingIns.isEmpty()) {
                InRequest inReq = waitingIns.removeFirst();
                inReq.matched = true;
                inReq.value = v;
                notifyAll();
                return;
            }

            outReq = new OutRequest(v);
            waitingOuts.addLast(outReq);

            if (!outObservers.isEmpty()) {
                toFire = new ArrayList<>(outObservers);
                outObservers.clear();
            }

            notifyAll();
        }

        if (toFire != null) {
            for (Observer o : toFire) {
                try {
                    o.update();
                } catch (Throwable ignored) {
                }
            }
        }

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
                immediate = !waitingIns.isEmpty();
                if (!immediate) {
                    inObservers.add(observer);
                }
            } else {
                immediate = !waitingOuts.isEmpty();
                if (!immediate) {
                    outObservers.add(observer);
                }
            }
        }

        if (immediate) {
            observer.update();
        }
    }

    public synchronized boolean isReady(Direction dir) {
        if (dir == Direction.In) {
            return !waitingOuts.isEmpty();
        } else {
            return !waitingIns.isEmpty();
        }
    }
}