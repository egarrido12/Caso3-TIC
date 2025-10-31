package actors;

import core.SystemState;
import java.util.Random;
import mailboxes.Mailbox;
import model.Message;
import model.MsgType;

/**
 * Consumidor de entrada (ESPERA PASIVA). Rutea:
 * - EMAIL spam -> cuarentena (put semiactivo, TTL [10000,20000])
 * - EMAIL válido -> entrega (put semiactivo)
 * - START / END -> control de estado (no son spam)
 * Decide envío de FIN a entrega y cuarentena cuando:
 *   entrada vacía && allClientsEnded && cuarentena vacía && no se haya enviado FIN.
 */
public class SpamFilter extends Thread {
    private final Mailbox inputBox;
    private final Mailbox quarantineBox;
    private final Mailbox deliveryBox;
    private final SystemState state;
    private final Random rnd = new Random();

    public SpamFilter(int id, Mailbox input, Mailbox quarantine, Mailbox delivery, SystemState st) {
        super("SpamFilter-" + id);
        this.inputBox = input;
        this.quarantineBox = quarantine;
        this.deliveryBox = delivery;
        this.state = st;
    }

    @Override
    public void run() {
        try {
            for (;;) {
                Message m = (Message) inputBox.take(); // PASIVA
                if (m.type == MsgType.START) {
                    // Solo informativo. SystemState ya contó onStart() desde el cliente.
                    continue;
                }
                if (m.type == MsgType.END) {
                    // Ya contado por el cliente (state.onEnd()).
                    trySendFINIfEligible();
                    continue;
                }
                // EMAIL:
                if (m.isSpam) {
                    m.quarantineTTL = 10000 + rnd.nextInt(10001); // [10000,20000]
                    quarantineBox.put(m); // semiactiva: no bloquea
                } else {
                    semiactivePut(deliveryBox, m);
                }
                trySendFINIfEligible();
            }
        } catch (InterruptedException ignored) {}
    }

    private void semiactivePut(Mailbox box, Message m) throws InterruptedException {
        // Reintentos si el buzón está lleno (bounded); breve sleep para semiactiva
        for (;;) {
            synchronized (box) {
                Message probe = box.poll();
                if (probe != null) {
                    // devolvemos lo que tomamos accidentalmente (raro que pase aquí).
                    try { box.put(probe); } catch (InterruptedException e) { throw e; }
                }
            }
            try {
                box.put(m);
                return;
            } catch (InterruptedException ie) { throw ie; }
            // Si hubiera una impl. que lanzara por lleno, aquí haríamos Thread.sleep(10);
        }
    }

    private void trySendFINIfEligible() throws InterruptedException {
        synchronized (state) {
            boolean eligible = inputBox.isEmpty()
                    && state.allClientsEnded()
                    && quarantineBox.isEmpty()
                    && !state.isFinSentToDelivery();
            if (eligible) {
                deliveryBox.put(Message.fin());
                state.markFinDelivery();
                quarantineBox.put(Message.fin());
                state.markFinQuarantine();
            }
        }
    }
}
