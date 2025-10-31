package cloudmail.actors;

import cloudmail.core.SystemState;
import cloudmail.mailboxes.Mailbox;
import cloudmail.model.Message;

import java.util.Random;

/**
 * Productor: genera START, N EMAILS (id único y flag spam aleatorio) y END.
 * Deposita en buzón de entrada (espera PASIVA).
 */
public class Client extends Thread {
    private final String clientId;
    private final int nEmails;
    private final Mailbox inputBox;
    private final SystemState state;
    private final Random rnd = new Random();

    public Client(String clientId, int nEmails, Mailbox inputBox, SystemState state) {
        super("Client-" + clientId);
        this.clientId = clientId;
        this.nEmails = nEmails;
        this.inputBox = inputBox;
        this.state = state;
    }

    @Override
    public void run() {
        try {
            inputBox.put(Message.start(clientId));
            state.onStart();
            for (int i = 1; i <= nEmails; i++) {
                boolean spam = rnd.nextBoolean();
                String mailId = clientId + "-" + i;
                inputBox.put(Message.email(clientId, mailId, spam)); // espera PASIVA si lleno
            }
            inputBox.put(Message.end(clientId));
            state.onEnd();
        } catch (InterruptedException ignored) {}
    }
}
