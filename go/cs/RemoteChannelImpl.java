package go.cs;

import go.shm.Channel;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteChannelImpl<T> extends UnicastRemoteObject implements RemoteChannel<T> {

    private final Channel<Object> channel;

    public RemoteChannelImpl(String name, go.shm.Factory factory) throws RemoteException {
        super();
        this.channel = (Channel<Object>) factory.newChannel(name);
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