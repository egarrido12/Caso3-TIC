package cloudmail.mailboxes;

import cloudmail.model.Message;

import java.util.ArrayDeque;

/**
 * Buzón SIN límite. Lectura con ESPERA SEMI-ACTIVA (wake-ups cortos).
 * - put no bloquea.
 * - take usa wait(50) para simular semiactiva.
 * - poll no bloquea.
 */
public class UnboundedMailbox implements Mailbox {
    private final ArrayDeque<Message> q = new ArrayDeque<>();

    @Override
    public synchronized void put(Message m) {
        q.addLast(m);
        notifyAll();
    }

    @Override
    public Message take() throws InterruptedException {
        for (;;) {
            synchronized (this) {
                if (!q.isEmpty()) {
                    return q.removeFirst();
                }
                // semiactiva: duermo poco, reviso de nuevo
                this.wait(50);
            }
        }
    }

    @Override
    public synchronized Message poll() {
        if (q.isEmpty()) return null;
        return q.removeFirst();
    }

    @Override public synchronized int size() { return q.size(); }
    @Override public synchronized boolean isEmpty() { return q.isEmpty(); }
}
