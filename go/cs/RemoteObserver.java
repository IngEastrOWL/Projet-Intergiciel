package go.cs;

import java.rmi.RemoteException;

public interface RemoteObserver {
    public void update() throws RemoteException;
}
