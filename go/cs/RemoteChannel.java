package go.cs;

import go.Direction;
import go.Observer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteChannel<T> extends Remote {

    public void out(T v) throws RemoteException;

    public T in() throws RemoteException;

    public String getName();

    public void observe(Direction direction, Observer observer);
}
