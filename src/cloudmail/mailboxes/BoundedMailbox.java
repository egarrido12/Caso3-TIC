package mailboxes;

import model.Message;

import java.util.ArrayDeque;

/**
 * Buzón de capacidad limitada con ESPERA PASIVA (wait/notifyAll).
 * - put/take bloquean si lleno/vacío respectivamente.
 * - poll es no bloqueante: devuelve null si vacío.
 */
public class BoundedMailbox implements Mailbox {
    private final ArrayDeque<Message> q = new ArrayDeque<>();
    private final int capacity;

    public BoundedMailbox(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    @Override
    public synchronized void put(Message m) throws InterruptedException {
        while (q.size() == capacity) wait(); // espera PASIVA (productor)
        q.addLast(m);
        notifyAll(); // despierta consumidores
    }

    @Override
    public synchronized Message take() throws InterruptedException {
        while (q.isEmpty()) wait(); // espera PASIVA (consumidor)
        Message m = q.removeFirst();
        notifyAll(); // despierta productores
        return m;
    }

    @Override
    public synchronized Message poll() {
        if (q.isEmpty()) return null;
        Message m = q.removeFirst();
        notifyAll();
        return m;
    }

    @Override public synchronized int size() { return q.size(); }
    @Override public synchronized boolean isEmpty() { return q.isEmpty(); }
}
