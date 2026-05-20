package go.cs;

import go.Direction;
import go.Observer;
import go.shm.Channel;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteChannelImpl<T> extends UnicastRemoteObject implements RemoteChannel<T> {

    private final Channel<Object> channel;
    private final String name;

    public RemoteChannelImpl(String name) throws RemoteException {
        super();
        this.channel = new Channel<>(name);
        this.name = name;
    }

    @Override
    public void out(T v) throws RemoteException {
        channel.out(v);
    }

    @Override
    public T in() throws RemoteException {
        return (T) channel.in();
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