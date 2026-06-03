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

        Channel<String> c1 = factory.newChannel("c1");

        new Thread(() -> {
            try { Thread.sleep(200); } catch (InterruptedException e) {}
            String msg = c1.in();

            if (!"RendezVousSocket".equals(msg)) {
                quit("KO: Mauvais message recu. Attendu 'RendezVousSocket', obtenu '" + msg + "'");
            }
        }).start();

        new Thread(() -> {
            try { Thread.sleep(800); } catch (InterruptedException e) {}
            c1.out("RendezVousSocket");

            // Si l'étape 1 passe sans deadlock, on enchaîne sur l'étape avancée
            try { Thread.sleep(200); } catch (InterruptedException e) {}
            runAdvancedChannelTransmissionTest(factory);
        }).start();
    }

    private static void runAdvancedChannelTransmissionTest(Factory factory) {

        Channel<Channel<Integer>> carrierChannel = factory.newChannel("carrier");

        Channel<Integer> dataChannel = factory.newChannel("dataChannel");

        new Thread(() -> {
            Channel<Integer> receivedChannel = carrierChannel.in();

            int value = receivedChannel.in();
            if (value != 42) {
                quit("KO: Mauvaise valeur reçue dans le canal transmis. Attendu 42, eu: " + value);
            }

            quit("ok");
        }).start();

        new Thread(() -> {
            try { Thread.sleep(400); } catch (InterruptedException e) {}

            carrierChannel.out(dataChannel);

            try { Thread.sleep(200); } catch (InterruptedException e) {}
            dataChannel.out(42);
        }).start();
    }
}