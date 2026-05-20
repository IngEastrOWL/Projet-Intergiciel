package go.cs;

import java.rmi.Remote;
import java.rmi.RemoteException;
import go.Direction;
import java.util.Map;


public interface RemoteChannelFactory extends Remote {
    RemoteChannel getChannel(String name) throws RemoteException;
    // Selectionne les channels sélectionnable
    String select(Map<String, Direction> channels) throws RemoteException;
}