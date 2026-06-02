package go.sock;

import go.Direction;
import go.Observer;

import java.io.*;
import java.net.Socket;

public class ChannelSlave<T> implements go.Channel<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    private final String name;
    private final String host;
    private final int port;

    public ChannelSlave(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
    }

    @Override
    public void out(T v) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            oos.writeObject("OUT");
            oos.writeObject(v);
            oos.flush();

            String response = ois.readObject().toString();
            if (!response.equals("OK")) {
                throw new RuntimeException("Erreur lors du traitement du out par le master");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur dans out", e);
        }
    }

    @Override
    public T in() {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            oos.writeObject("IN");
            oos.flush();

            String response = ois.readObject().toString();
            if (response.equals("OK")) {
                return (T) ois.readObject();
            }
            throw new RuntimeException("Erreur lors du traitement du in par le master");
        } catch (Exception e) {
            throw new RuntimeException("Erreur dans out", e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void observe(Direction direction, Observer observer) {
        System.out.println("pas le courage de faire le observe");
    }
}
