package go.cs;

import go.shm.Channel;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteChannelImpl<T> extends UnicastRemoteObject implements RemoteChannel<T> {

    private final Channel<Object> channel;

    public RemoteChannelImpl(String name) throws RemoteException {
        super();
        this.channel = new Channel<>(name);
    }

    @Override
    public void out(T v) throws RemoteException {
        channel.out(v);
    }

    @Override
    public T in() throws RemoteException {
        return (T) channel.in();
    }
}