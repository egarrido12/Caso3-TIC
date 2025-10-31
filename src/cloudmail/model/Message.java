package model;

public class Message {
    public final MsgType type;
    public final String clientId;   // para START/END/EMAIL
    public final String mailId;     // único: clientId + secuencial (solo EMAIL)
    public final boolean isSpam;    // flag de spam (solo EMAIL, en START/END/FIN es false)

    // Solo para cuarentena:
    public int quarantineTTL;       // en ms (ticks simulados de 1000 ms)

    public Message(MsgType type, String clientId, String mailId, boolean isSpam, int ttl) {
        this.type = type;
        this.clientId = clientId;
        this.mailId = mailId;
        this.isSpam = isSpam;
        this.quarantineTTL = ttl;
    }

    public static Message start(String clientId) {
        return new Message(MsgType.START, clientId, null, false, 0);
    }

    public static Message end(String clientId) {
        return new Message(MsgType.END, clientId, null, false, 0);
    }

    public static Message email(String clientId, String mailId, boolean spam) {
        return new Message(MsgType.EMAIL, clientId, mailId, spam, 0);
    }

    public static Message fin() {
        return new Message(MsgType.FIN, "SYSTEM", null, false, 0);
    }

    @Override
    public String toString() {
        return "Message{" + type +
                (clientId != null ? ", client=" + clientId : "") +
                (mailId != null ? ", id=" + mailId : "") +
                (type == MsgType.EMAIL ? ", spam=" + isSpam : "") +
                (quarantineTTL > 0 ? ", ttl=" + quarantineTTL : "") +
                "}";
    }
}

