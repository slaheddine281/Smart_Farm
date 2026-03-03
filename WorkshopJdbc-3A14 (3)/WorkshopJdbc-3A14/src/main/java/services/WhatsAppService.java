package services;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import utils.TwilioConfig;

public class WhatsAppService {

    // ✅ Constructor simplifié - pas d'instanciation de TwilioConfig
    public WhatsAppService() {
        // Initialiser Twilio (méthode statique)
        TwilioConfig.init();
        System.out.println("✅ WhatsApp Service prêt !");
    }

    /**
     * Envoyer un message WhatsApp
     * @param toNumber Numéro du destinataire (ex: +21650093975)
     * @param body Contenu du message
     * @return true si envoyé avec succès
     */
    public boolean sendWhatsAppMessage(String toNumber, String body) {
        try {
            // ✅ Formater les numéros
            String formattedTo = formatForTwilio(toNumber);
            String formattedFrom = TwilioConfig.getWhatsAppNumber();

            System.out.println("📤 Envoi WhatsApp...");
            System.out.println("   De: " + formattedFrom);
            System.out.println("   Vers: " + formattedTo);

            Message message = Message.creator(
                    new PhoneNumber(formattedTo),
                    new PhoneNumber(formattedFrom),
                    body
            ).create();

            System.out.println("✅ Message WhatsApp envoyé ! SID: " + message.getSid());
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi WhatsApp: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envoyer une notification de tâche assignée
     */
    public void sendTaskNotification(String employeePhone, String employeeName,
                                     String taskDescription, String taskDate) {
        String message = "🔔 *NOUVELLE TÂCHE - SMART FARM*\n\n" +
                "👤 *Employé:* " + employeeName + "\n" +
                "📋 *Tâche:* " + taskDescription + "\n" +
                "📅 *Date:* " + taskDate + "\n\n" +
                "_Smart Farm Management System_";

        sendWhatsAppMessage(employeePhone, message);
    }

    /**
     * Formater un numéro pour l'API Twilio
     */
    private String formatForTwilio(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("Numéro de téléphone requis");
        }

        // Supprimer espaces, tirets, parenthèses
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)]", "");

        // Ajouter + si manquant
        if (!cleaned.startsWith("+")) {
            cleaned = "+" + cleaned;
        }

        // Ajouter préfixe whatsapp: pour l'API Twilio
        return "whatsapp:" + cleaned;
    }

    /**
     * Méthode utilitaire pour tester la connexion Twilio
     */
    public boolean isServiceAvailable() {
        try {
            // Juste vérifier que Twilio est initialisé
            TwilioConfig.getWhatsAppNumber();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}