package go.shm;

import go.Direction;
import go.Observer;

import java.util.ArrayList;

public class Channel<T> implements go.Channel<T> {

    private String name;
    private T value;
    private boolean hasValue = false;
    private int waitingCountOut = 0;
    private int waitingCountIn = 0;

    public ArrayList<Observer> observersIn = new ArrayList<>();
    public ArrayList<Observer> observersOut = new ArrayList<>();

    public Channel(String name) { this.name = name; }
    
    public synchronized void out(T v) {
        try {
            waitingCountOut++;
            while (!observersOut.isEmpty()) {
                observersOut.removeLast().update();
            }
            while (hasValue) { // tant que le canal est occupé par un out, on attend
                wait();
            }
            value = v;
            hasValue = true;
            notifyAll();
            waitingCountOut--;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            notifyAll();
        }
    }
    
    public synchronized T in() {
        try {
            waitingCountIn++;
            while (!observersIn.isEmpty()) {
                observersIn.removeLast().update();
            }
            while (!hasValue) { // tant que le canal est vide, on attend
                wait();
            }
            T res = value;
            hasValue = false;
            notifyAll();
            waitingCountIn--;
            return res;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            notifyAll();
        }
    }

    public String getName() { return this.name; }

    public synchronized void observe(Direction dir, Observer observer) {
        if (dir == Direction.Out && waitingCountOut > 0) {
            observer.update();
        } else if (dir == Direction.In && waitingCountIn > 0) {
            observer.update();
        } else {
            switch (dir){
                case Direction.In -> observersIn.add(observer);
                case Direction.Out -> observersOut.add(observer);
            }
        }
    }
        
}
