package go.sock;

import go.Direction;
import go.Observer;
import go.shm.Factory;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ChannelMaster<T> implements go.Channel<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String host;
    private final int port;

    private transient final go.shm.Channel<T> shmChannel;
    private transient ServerSocket serverSocket;

    public ChannelMaster(String name, go.shm.Factory shmFactory) throws Exception {
        this.name = name;

        this.shmChannel = (go.shm.Channel<T>) shmFactory.newChannel(name);

        this.serverSocket = new ServerSocket(0);
        this.host = InetAddress.getLocalHost().getHostAddress();
        this.port = serverSocket.getLocalPort();

        Naming.register(name, host, port);
        new Thread(this::listen).start();
    }

    private void listen() {
        while (serverSocket != null && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            } catch (Exception e) {
                break;
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {
            String command = ois.readObject().toString();

            if (command.equals("OUT")) {
                T value = (T) ois.readObject();
                shmChannel.out(value);
                oos.writeObject("OK");
                oos.flush();
            } else if (command.equals("IN")) {
                T value = shmChannel.in();
                oos.writeObject("OK");
                oos.writeObject(value);
                oos.flush();
            }
        } catch (Exception e) {}
    }

    @Override
    public void out(T v) {
        shmChannel.out(v);
    }

    @Override
    public T in() {
        return shmChannel.in();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void observe(Direction direction, Observer observer) {
        System.out.println("toujours pas le courage");
    }
}
