package go.shm;

import go.Direction;
import go.Observer;

public class Channel<T> implements go.Channel<T> {

    private String name;
    private T value;
    private boolean hasValue = false;

    public Channel(String name) { this.name = name; }
    
    public synchronized void out(T v) {
        try {
            while (hasValue) { // tant que le canal est occupé par un out, on attend
                wait();
            }
            value = v;
            hasValue = true;
            notifyAll();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            notifyAll();
        }
    }
    
    public synchronized T in() {
        try {
            while (!hasValue) { // tant que le canal est vide, on attend
                wait();
            }
            T res = value;
            hasValue = false;
            notifyAll();
            return res;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            notifyAll();
        }
    }

    public String getName() { return this.name; }

    public void observe(Direction dir, Observer observer) {
        // TODO
    }
        
}
