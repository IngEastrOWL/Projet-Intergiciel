package go.sock;

import go.Direction;
import go.Observer;
import go.shm.Factory;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Composant Maître (Serveur). Il gère le canal en mémoire partagée locale (shm)
 * et ouvre un ServerSocket pour permettre aux ChannelSlaves distants d'y accéder.
 */
public class ChannelMaster<T> implements go.Channel<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String host;
    private final int port;

    // Champs marqués 'transient' pour être ignorés lors de la sérialisation
    private transient final go.shm.Channel<T> shmChannel;
    private transient ServerSocket serverSocket;

    public ChannelMaster(String name, go.shm.Factory shmFactory) throws Exception {
        this.name = name;

        // initialisation du canal en mémoire partagée
        this.shmChannel = (go.shm.Channel<T>) shmFactory.newChannel(name);

        // '0' permet d'allouer automatiquement un port libre
        this.serverSocket = new ServerSocket(0);
        this.host = InetAddress.getLocalHost().getHostAddress();
        this.port = serverSocket.getLocalPort();

        Naming.register(name, host, port);
        new Thread(this::listen).start();
    }

    // boucle d'écoute principale du serveur
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

    // Traite les requêtes ("IN" ou "OUT") envoyées par un ChannelSlave
    private void handleClient(Socket clientSocket) {
        try (ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {
            String command = ois.readObject().toString();

            if (command.equals("OUT")) {
                T value = (T) ois.readObject();
                // Réceptionne la donnée et l'ajoute dans la mémoire partagée
                shmChannel.out(value);
                oos.writeObject("OK");
                oos.flush();
            } else if (command.equals("IN")) {
                // récupère la donnée pour la renvoyer au slave
                T value = shmChannel.in();
                oos.writeObject("OK");
                oos.writeObject(value);
                oos.flush();
            }
        } catch (Exception e) {}
    }

    // Si le processus master utilise lui-même le canal il tape directement dans la mémoire partagée.

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