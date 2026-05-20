package go.cs;

import go.Direction;
import go.Observer;
import go.shm.Channel;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteChannelImpl<T> extends UnicastRemoteObject implements RemoteChannel<T> {

    private final Channel<T> channel;
    private final String name;

    public RemoteChannelImpl(String name, go.shm.Factory factory) throws RemoteException {
        super();
        this.channel = (Channel<T>) factory.newChannel(name);
        this.name = name;
    }

    @Override
    public void out(T v) throws RemoteException {
        channel.out(v);
    }

    @Override
    public T in() throws RemoteException {
        return channel.in();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void observe(Direction direction, Observer observer) {
        channel.observe(direction, observer);
    }
}