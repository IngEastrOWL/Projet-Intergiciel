package go.test;

import go.*;
import go.sock.Naming;

public class TestSock01 {

    private static void quit(String msg) {
        System.out.println("TestSock01: " + msg);
        System.exit(msg.equals("ok") ? 0 : 1);
    }

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                Naming.main(new String[0]);
            } catch (Exception e) {
                quit("KO: Impossible de lancer le service Naming: " + e.getMessage());
            }
        }).start();

        try { Thread.sleep(500); } catch (InterruptedException e) {}

        new Thread(() -> {
            try { Thread.sleep(5000); } catch (InterruptedException e) {}
            quit("KO (deadlock)");
        }).start();

        Factory factory = new go.sock.Factory();

        System.out.println("--- ÉTAPE 1: Test du in/out synchrone standard ---");
        Channel<String> c1 = factory.newChannel("c1");

        new Thread(() -> {
            try { Thread.sleep(200); } catch (InterruptedException e) {}
            System.out.println("[Client IN] Tentative de lecture sur c1 (va bloquer)...");
            String msg = c1.in();
            System.out.println("[Client IN] Débloqué ! Message reçu: " + msg);

            if (!"RendezVousSocket".equals(msg)) {
                quit("KO: Mauvais message recu. Attendu 'RendezVousSocket', obtenu '" + msg + "'");
            }
        }).start();

        new Thread(() -> {
            try { Thread.sleep(800); } catch (InterruptedException e) {}
            System.out.println("[Client OUT] Envoi du message sur c1...");
            c1.out("RendezVousSocket");
            System.out.println("[Client OUT] Envoi validé par le rendez-vous !");

            // Si l'étape 1 passe sans deadlock, on enchaîne sur l'étape avancée
            try { Thread.sleep(200); } catch (InterruptedException e) {}
            runAdvancedChannelTransmissionTest(factory);
        }).start();
    }

    private static void runAdvancedChannelTransmissionTest(Factory factory) {
        System.out.println("\n--- ÉTAPE 2: Test de l'envoi d'un canal dans un canal ---");

        Channel<Channel<Integer>> carrierChannel = factory.newChannel("carrier");

        Channel<Integer> dataChannel = factory.newChannel("dataChannel");

        new Thread(() -> {
            System.out.println("[Carrier IN] Attend la réception d'un canal...");
            Channel<Integer> receivedChannel = carrierChannel.in();
            System.out.println("[Carrier IN] Canal reçu ! Nom: " + receivedChannel.getName());

            System.out.println("[Received IN] Attend la valeur 42 dans le canal reçu...");
            int value = receivedChannel.in();
            if (value != 42) {
                quit("KO: Mauvaise valeur reçue dans le canal transmis. Attendu 42, eu: " + value);
            }

            quit("ok");
        }).start();

        new Thread(() -> {
            try { Thread.sleep(400); } catch (InterruptedException e) {}

            System.out.println("[Carrier OUT] Envoie l'objet dataChannel...");
            carrierChannel.out(dataChannel);

            try { Thread.sleep(200); } catch (InterruptedException e) {}
            System.out.println("[Data OUT] Pousse la valeur 42 dans dataChannel...");
            dataChannel.out(42);
        }).start();
    }
}