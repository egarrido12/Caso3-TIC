package cloudmail.config;

public class MailConfig {
    public final int numClients;
    public final int emailsPerClient;
    public final int numFilters;
    public final int numServers;
    public final int inputCapacity;
    public final int deliveryCapacity;

    private MailConfig(int numClients, int emailsPerClient, int numFilters, int numServers,
                       int inputCapacity, int deliveryCapacity) {
        this.numClients = numClients;
        this.emailsPerClient = emailsPerClient;
        this.numFilters = numFilters;
        this.numServers = numServers;
        this.inputCapacity = inputCapacity;
        this.deliveryCapacity = deliveryCapacity;
    }

    public static MailConfig defaultConfig() {
        // Puedes ajustar números para forzar esperas y probar sincronización
        return new MailConfig(
                3,   // numClients
                8,   // emailsPerClient
                2,   // numFilters
                2,   // numServers
                8,   // inputCapacity (bounded, espera PASIVA)
                8    // deliveryCapacity (bounded; escritura semiactiva, lectura activa)
        );
    }
}
