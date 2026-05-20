package go.cs;

import go.Direction;
import go.Observer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Channel<T> implements go.Channel<T> {

    private String name;
    private RemoteChannel channel;

    public Channel(String name) {
        this.name = name;
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            RemoteChannelFactory server = (RemoteChannelFactory) registry.lookup("ChannelFactory");
            this.channel = server.getChannel(this.name);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to RMI Server", e);
        }
    }

    public void out(T v) {
        try {
            channel.out(v);
        } catch (Exception e) {
            throw new RuntimeException("out error");
        }
    }

    public T in() {
        try {
            return (T) channel.in();
        } catch (Exception e) {
            throw new RuntimeException("in error");
        }
    }

    public String getName() {
        return name;
    }

    public void observe(Direction direction, Observer observer) {
        try {
            channel.observe(direction, observer);
        } catch (Exception e) {
            throw new RuntimeException("observe error");
        }
    }
}
