package cloudmail.actors;

import cloudmail.mailboxes.Mailbox;
import cloudmail.model.Message;
import cloudmail.model.MsgType;

import java.util.Random;

/**
 * Lee de cuarentena en ESPERA SEMI-ACTIVA (buzón semiactivo), cada ~1s:
 * - Decrementa TTL en 1000 ms.
 * - Si TTL llega a 0: descarta con prob. 3/21 (múltiplo de 7) o envía a entrega (semiactiva).
 * - Termina al recibir FIN.
 */
public class QuarantineManager extends Thread {
    private final Mailbox quarantine;
    private final Mailbox delivery;
    private volatile boolean running = true;
    private final Random rnd = new Random();

    public QuarantineManager(Mailbox quarantine, Mailbox delivery) {
        super("QuarantineManager");
        this.quarantine = quarantine;
        this.delivery = delivery;
    }

    @Override
    public void run() {
        try {
            while (running) {
                Message m = quarantine.take(); // semiactiva (impl del buzón usa wait corto)
                if (m.type == MsgType.FIN) {
                    running = false;
                    break;
                }
                // Decremento de TTL
                m.quarantineTTL = Math.max(0, m.quarantineTTL - 1000);
                if (m.quarantineTTL > 0) {
                    quarantine.put(m);
                } else {
                    int k = 1 + rnd.nextInt(21);
                    if (k % 7 != 0) {
                        delivery.put(m); // semiactiva en escritura
                    }
                    // si es múltiplo de 7, se descarta
                }
                Thread.sleep(1000); // ritmo de 1 segundo
            }
        } catch (InterruptedException ignored) {}
    }
}
