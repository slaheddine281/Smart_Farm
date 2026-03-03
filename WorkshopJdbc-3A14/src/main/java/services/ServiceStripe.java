package services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

/**
 * Service Stripe — paiement en mode test.
 */
public class ServiceStripe {

    private static final String STRIPE_SECRET_KEY =
            "sk_test_51T5HiQ60xPrkPbKSFgEU1e3IoC3ONyG8Obwc7vrnNkdRcKgpBZ6PUEQrPOTQzRH6y2UpxU5EHuHDdXmZvt54dgbh00vH7Dz1xY";

    public ServiceStripe() {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }

    /**
     * Confirme le paiement en mode test avec la carte de test Stripe.
     * Carte test : 4242 4242 4242 4242 / exp: 12/34 / CVC: 123
     *
     * @param montantEuros  montant à payer
     * @param description   description de la commande
     * @return              ID du PaymentIntent confirmé (pi_xxx...)
     */
    public String payerEnModeTest(double montantEuros, String description) throws StripeException {
        long montantCentimes = Math.round(montantEuros * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(montantCentimes)
                .setCurrency("eur")
                .setDescription(description)
                .setConfirm(true)
                .setPaymentMethod("pm_card_visa")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(
                                        PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER
                                )
                                .build()
                )
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        System.out.println("✅ Paiement Stripe - ID: " + intent.getId()
                + " | Statut: " + intent.getStatus());
        return intent.getId();
    }
}