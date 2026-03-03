package utils;

import com.twilio.Twilio;

public class TwilioConfig {

    // Vos credentials Twilio
    private static final String ACCOUNT_SID = "ACd1534de5c31cdb362a58a2d58ec35221";
    private static final String AUTH_TOKEN = "bce856dcee00debd2a742e94d2ad48ad";
    private static final String WHATSAPP_NUMBER = "whatsapp:+14155238886";

    // Flag pour éviter la double initialisation
    private static boolean initialized = false;

    /**
     * Initialiser Twilio manuellement (à appeler UNE FOIS au démarrage)
     */
    public static void init() {
        if (!initialized) {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            initialized = true;
            System.out.println("✅ Twilio initialisé avec succès !");
        }
    }

    /**
     * Vérifier si Twilio est initialisé
     */
    public static boolean isInitialized() {
        return initialized;
    }

    public static String getWhatsAppNumber() {
        return WHATSAPP_NUMBER;
    }
}