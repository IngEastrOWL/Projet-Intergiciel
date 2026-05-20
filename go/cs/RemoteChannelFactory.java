package go.cs;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteChannelFactory extends Remote {
    RemoteChannel getChannel(String name) throws RemoteException;
}