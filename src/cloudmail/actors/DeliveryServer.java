package actors;

import mailboxes.Mailbox;
import model.Message;
import model.MsgType;

import java.util.Random;

/**
 * Consumidor en ESPERA ACTIVA sobre el buzón de entrega:
 * - Usa poll() no bloqueante en bucle con Thread.yield() para simular activa.
 * - Procesa EMAIL con sleep corto aleatorio.
 * - Termina al leer FIN.
 */
public class DeliveryServer extends Thread {
    private final Mailbox delivery;
    private final Random rnd = new Random();

    public DeliveryServer(int id, Mailbox delivery) {
        super("DeliveryServer-" + id);
        this.delivery = delivery;
    }

    @Override
    public void run() {
        try {
            for (;;) {
                Message m = delivery.poll(); // ACTIVA: no bloqueante
                if (m == null) {
                    Thread.yield();
                    continue;
                }
                if (m.type == MsgType.FIN) break;

                // Procesamiento simulado
                Thread.sleep(50 + rnd.nextInt(100));
            }
        } catch (InterruptedException ignored) {}
    }
}
