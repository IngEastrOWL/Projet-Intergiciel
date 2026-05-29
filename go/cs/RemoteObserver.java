package go.cs;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObserver extends Remote {
    public void update() throws RemoteException;
}
