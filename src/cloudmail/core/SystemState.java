package cloudmail.core;

/**
 * Estado global sincronizado para coordinar FIN y contadores START/END.
 */
public class SystemState {
    private int totalClients = 0;
    private int startSeen = 0; // opcional, por si quieres validación de arranque
    private int endSeen = 0;

    private boolean finSentToDelivery = false;
    private boolean finSentToQuarantine = false;

    public synchronized void setTotalClients(int n) { totalClients = n; }
    public synchronized int getTotalClients() { return totalClients; }

    public synchronized void onStart() { startSeen++; }
    public synchronized int getStartSeen() { return startSeen; }

    public synchronized void onEnd() { endSeen++; }
    public synchronized int getEndSeen() { return endSeen; }
    public synchronized boolean allClientsEnded() { return endSeen >= totalClients; }

    public synchronized boolean isFinSentToDelivery() { return finSentToDelivery; }
    public synchronized void markFinDelivery() { finSentToDelivery = true; }

    public synchronized boolean isFinSentToQuarantine() { return finSentToQuarantine; }
    public synchronized void markFinQuarantine() { finSentToQuarantine = true; }
}


