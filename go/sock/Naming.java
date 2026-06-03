package go.sock;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Naming {

    private static final String HOST = "localhost";
    private static final int PORT = 2000;

    // Enregistre un service (nom, hôte, port) auprès du serveur de nommage
    public static synchronized void register(String name, String host, int port) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println("REGISTER " + name + " " + host + " " + port);
            String response = in.readLine();
            if (!"OK".equals(response)) {
                throw new RuntimeException("enregistrement fail auprès du Naming");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Récupère l'adresse (hôte:port) d'un service à partir de son nom
    public static synchronized String lookup(String name) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println("LOOKUP " + name);
            String response = in.readLine();
            if (response == null || response.equals("NOT_FOUND")) {
                return null;
            }
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String args[]) throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);

        // Table de hachage partagée stockant l'annuaire (Nom -> Hôte:Port)
        Map<String, String> registry = new HashMap<>();

        // Boucle principale d'écoute du serveur
        while (true) {
            Socket clientSocket = serverSocket.accept();

            // Gestion de chaque client dans un Thread dédié pour ne pas bloquer le serveur
            new Thread(() -> {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String line = in.readLine();
                    if (line == null) return;

                    String[] tokens = line.split(" ");
                    String command = tokens[0];

                    // Cas 1 : Demande d'enregistrement
                    if (command.equals("REGISTER")) {
                        String name = tokens[1];
                        String host = tokens[2];
                        String port = tokens[3];

                        // Bloc synchronisé pour éviter les conflits d'écriture sur la Map
                        synchronized (registry) {
                            registry.put(name, host+":"+port);
                        }
                        out.println("OK");
                        System.out.println("[Naming] Nouveau canal enregistré : " + name + " -> " + host + ":" + port);

                        // Cas 2 : Demande de recherche
                    } else if (command.equals("LOOKUP")) {
                        String name = tokens[1];
                        String channel;

                        // Bloc synchronisé pour garantir une lecture safe pendant les écritures
                        synchronized (registry) {
                            channel = registry.get(name);
                        }
                        if (channel != null) {
                            out.println(channel);
                        } else {
                            out.println("NOT_FOUND");
                        }
                    } else {
                        out.println("ERROR");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }
}