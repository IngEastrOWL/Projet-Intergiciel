package go.cs;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteChannel<T> extends Remote {

    public void out(T v) throws RemoteException;

    public T in() throws RemoteException;
}
