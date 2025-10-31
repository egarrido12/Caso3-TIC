package cloudmail;

import cloudmail.actors.*;
import cloudmail.config.MailConfig;
import cloudmail.core.SystemState;
import cloudmail.mailboxes.*;
import cloudmail.model.Message;

public class App {
    public static void main(String[] args) throws InterruptedException {
        // 1) Configuración
        MailConfig cfg = MailConfig.defaultConfig();

        // 2) Estado global (contadores y banderas de FIN)
        SystemState state = new SystemState();
        state.setTotalClients(cfg.numClients);

        // 3) Buzones
        Mailbox input = new BoundedMailbox(cfg.inputCapacity);     // espera PASIVA
        Mailbox quarantine = new UnboundedMailbox();               // espera SEMI-ACTIVA
        Mailbox delivery = new BoundedMailbox(cfg.deliveryCapacity);// semiactiva para escribir, activa para leer

        // 4) Actores
        QuarantineManager qm = new QuarantineManager(quarantine, delivery);
        qm.start();

        DeliveryServer[] servers = new DeliveryServer[cfg.numServers];
        for (int i = 0; i < cfg.numServers; i++) {
            servers[i] = new DeliveryServer(i + 1, delivery);
            servers[i].start();
        }

        SpamFilter[] filters = new SpamFilter[cfg.numFilters];
        for (int i = 0; i < cfg.numFilters; i++) {
            filters[i] = new SpamFilter(i + 1, input, quarantine, delivery, state);
            filters[i].start();
        }

        Client[] clients = new Client[cfg.numClients];
        for (int i = 0; i < cfg.numClients; i++) {
            clients[i] = new Client("C" + (i + 1), cfg.emailsPerClient, input, state);
            clients[i].start();
        }

        // 5) Replicador de FIN para servidores: cuando el primer FIN se envíe a entrega, se clona para todos
        new Thread(() -> {
            boolean done = false;
            while (!done) {
                synchronized (state) {
                    if (state.isFinSentToDelivery()) {
                        try {
                            for (int i = 0; i < cfg.numServers; i++) {
                                delivery.put(Message.fin());
                            }
                        } catch (InterruptedException ignored) {}
                        done = true;
                    }
                }
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        }, "FinReplicator").start();

        // 6) Esperar cierre ordenado
        for (Client c : clients) c.join();
        for (SpamFilter f : filters) f.join();
        qm.join();
        for (DeliveryServer s : servers) s.join();

        // 7) Invariante final: todos los buzones vacíos
        if (!input.isEmpty() || !quarantine.isEmpty() || !delivery.isEmpty()) {
            System.err.println("Error: algún buzón NO quedó vacío al terminar.");
        } else {
            System.out.println("Sistema terminado con éxito. Todos los buzones vacíos.");
        }
    }
}
