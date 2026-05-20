package go.cs;

import go.Observer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteObserverImpl extends UnicastRemoteObject implements RemoteObserver {

    private final Observer observer;

    public RemoteObserverImpl(Observer observer) throws RemoteException {
        super();
        this.observer = observer;
    }

    @Override
    public void update() {
        observer.update();
    }
}

