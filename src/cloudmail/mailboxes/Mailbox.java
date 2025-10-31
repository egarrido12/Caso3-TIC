package mailboxes;

import model.Message;

/**
 * Interfaz de buzón.
 * - put/take: comportamiento bloqueante o semiactivo según implementación.
 * - poll(): no bloqueante; devuelve null si vacío (útil para espera ACTIVA).
 */
public interface Mailbox {
    void put(Message m) throws InterruptedException;
    Message take() throws InterruptedException;

    // No bloqueante (para servidores en espera activa):
    Message poll();

    int size();
    boolean isEmpty();
}
