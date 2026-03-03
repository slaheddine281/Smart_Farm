import services.WhatsAppService;

public class TestWhatsApp {
    public static void main(String[] args) {
        System.out.println("🚀 Test WhatsApp Sandbox - Smart Farm\n");
        System.out.println("=".repeat(50));

        try {
            // 1. Initialiser le service
            System.out.println("\n1️⃣ Initialisation du service WhatsApp...");
            WhatsAppService service = new WhatsAppService();
            System.out.println("✅ Service prêt !");

            // 2. Votre numéro (vérifié pour le sandbox)
            String myNumber = "+21650093975";
            System.out.println("\n2️⃣ Numéro de test : " + myNumber);

            // 3. Message de test
            String testMessage = "🎉 *TEST RÉUSSI !*\n\n" +
                    "Votre intégration WhatsApp Smart Farm fonctionne !\n\n" +
                    "📅 Date: " + java.time.LocalDate.now() + "\n" +
                    " Projet: WorkshopJdbc-3A14\n\n" +
                    "_Bravo Ahmed !_";

            // 4. Envoyer le message
            System.out.println("\n3️⃣ Envoi du message WhatsApp...");
            boolean sent = service.sendWhatsAppMessage(myNumber, testMessage);

            // 5. Résultat
            System.out.println("\n" + "=".repeat(50));
            if (sent) {
                System.out.println("✅ MESSAGE ENVOYÉ AVEC SUCCÈS !");
                System.out.println("📱 Vérifiez votre WhatsApp !");
            } else {
                System.out.println("❌ ÉCHEC DE L'ENVOI");
            }
            System.out.println("=".repeat(50));

        } catch (Exception e) {
            System.err.println("\n❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
