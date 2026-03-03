package services;

import entities.Commande;
import entities.CommandeItem;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

/**
 * Service d'envoi d'email de confirmation de paiement via Gmail SMTP.
 *
 * ⚠️ CONFIGURATION REQUISE (une seule fois) :
 * 1. Allez sur https://myaccount.google.com/security
 * 2. Activez "Validation en 2 étapes"
 * 3. Allez dans "Mots de passe des applications"
 * 4. Créez un mot de passe pour "Autre (nom personnalisé)" → "SmartFarm"
 * 5. Copiez le mot de passe de 16 caractères et collez-le dans GMAIL_PASSWORD
 */
public class ServiceMail {

    // ⚠️ REMPLACEZ PAR VOTRE EMAIL ET MOT DE PASSE D'APPLICATION GMAIL
    private static final String GMAIL_USER     = "abdouelouear321@gmail.com";
    private static final String GMAIL_PASSWORD = "rtsj nmbf zlly uygh"; // mot de passe d'application (16 car.)

    private static final Map<String, String> UNITES = Map.of(
            "Lait",   "L",
            "Œufs",  "unité(s)",
            "Laine",  "kg",
            "Viande", "kg"
    );

    /**
     * Envoie un email HTML de confirmation de paiement.
     *
     * @param destinataire  email du client (ex: client@gmail.com)
     * @param commande      objet Commande avec les articles
     * @param stripeId      ID de transaction Stripe (pi_xxx...)
     */
    public void envoyerConfirmation(String destinataire, Commande commande, String stripeId) throws MessagingException, UnsupportedEncodingException {
        Session session = creerSession();
        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(GMAIL_USER, "Smart Farm 🌱", "UTF-8"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
        message.setSubject("✅ Confirmation de votre commande — Smart Farm");
        message.setContent(buildHtml(commande, stripeId), "text/html; charset=UTF-8");

        Transport.send(message);
        System.out.println("✅ Email envoyé à : " + destinataire);
    }

    // ── Session SMTP Gmail ───────────────────────
    private Session creerSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.protocols",   "TLSv1.2");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GMAIL_USER, GMAIL_PASSWORD);
            }
        });
    }

    // ── Construction du HTML ─────────────────────
    private String buildHtml(Commande commande, String stripeId) {
        StringBuilder lignes = new StringBuilder();
        for (CommandeItem item : commande.getItems()) {
            String unite = UNITES.getOrDefault(item.getProductionType(), "");
            lignes.append(String.format("""
                <tr>
                  <td style="padding:10px 14px; border-bottom:1px solid #ECF0F1;">
                    %s
                  </td>
                  <td style="padding:10px 14px; border-bottom:1px solid #ECF0F1; text-align:center;">
                    %.2f %s
                  </td>
                  <td style="padding:10px 14px; border-bottom:1px solid #ECF0F1; text-align:right;">
                    %.2f €
                  </td>
                  <td style="padding:10px 14px; border-bottom:1px solid #ECF0F1; text-align:right; font-weight:bold; color:#27AE60;">
                    %.2f €
                  </td>
                </tr>
                """,
                    item.getProductionType(),
                    item.getQuantiteAchat(), unite,
                    item.getPrixUnitaire(),
                    item.getSousTotal()
            ));
        }

        // Stripe ID tronqué pour l'affichage
        String stripeDisplay = (stripeId != null && stripeId.length() > 20)
                ? stripeId.substring(0, 20) + "..."
                : (stripeId != null ? stripeId : "N/A");

        return String.format("""
            <!DOCTYPE html>
            <html lang="fr">
            <head><meta charset="UTF-8"/></head>
            <body style="margin:0; padding:0; background-color:#F0F4F8; font-family:'Segoe UI', Arial, sans-serif;">

              <!-- HEADER -->
              <div style="background: linear-gradient(135deg,#1A8A4A,#27AE60,#2ECC71);
                          padding:40px 30px; text-align:center;">
                <h1 style="color:white; margin:0; font-size:28px;">🌱 Smart Farm</h1>
                <p style="color:rgba(255,255,255,0.85); margin:8px 0 0; font-size:15px;">
                  Confirmation de paiement
                </p>
              </div>

              <!-- BODY -->
              <div style="max-width:600px; margin:30px auto; background:white;
                          border-radius:16px; overflow:hidden;
                          box-shadow:0 4px 20px rgba(0,0,0,0.08);">

                <!-- Message de succès -->
                <div style="background:#E8F8F0; padding:24px 30px; border-left:5px solid #27AE60;">
                  <h2 style="color:#1E8449; margin:0 0 6px;">✅ Paiement confirmé !</h2>
                  <p style="color:#27AE60; margin:0; font-size:14px;">
                    Merci pour votre commande. Votre paiement a bien été reçu.
                  </p>
                </div>

                <!-- ID Transaction Stripe -->
                <div style="padding:20px 30px; background:#F8F9FA; border-bottom:1px solid #ECF0F1;">
                  <p style="margin:0; font-size:12px; color:#95A5A6; font-weight:bold; text-transform:uppercase;">
                    🔑 ID Transaction Stripe
                  </p>
                  <p style="margin:6px 0 0; font-family:'Courier New', monospace;
                             font-size:13px; color:#2C3E50; font-weight:bold;
                             background:#ECF0F1; padding:8px 12px; border-radius:6px;">
                    %s
                  </p>
                </div>

                <!-- Tableau des articles -->
                <div style="padding:24px 30px;">
                  <h3 style="color:#2C3E50; margin:0 0 16px; font-size:16px;">
                    📦 Détail de votre commande
                  </h3>
                  <table style="width:100%%; border-collapse:collapse; font-size:14px;">
                    <thead>
                      <tr style="background:linear-gradient(to right,#27AE60,#2ECC71);">
                        <th style="padding:12px 14px; color:white; text-align:left; border-radius:8px 0 0 0;">Produit</th>
                        <th style="padding:12px 14px; color:white; text-align:center;">Quantité</th>
                        <th style="padding:12px 14px; color:white; text-align:right;">Prix unit.</th>
                        <th style="padding:12px 14px; color:white; text-align:right; border-radius:0 8px 0 0;">Sous-total</th>
                      </tr>
                    </thead>
                    <tbody>
                      %s
                    </tbody>
                  </table>
                </div>

                <!-- Total -->
                <div style="margin:0 30px 24px; background:linear-gradient(135deg,#27AE60,#1E8449);
                            border-radius:12px; padding:20px 24px;
                            display:flex; justify-content:space-between; align-items:center;">
                  <span style="color:rgba(255,255,255,0.85); font-size:14px; font-weight:bold;">
                    TOTAL PAYÉ
                  </span>
                  <span style="color:white; font-size:28px; font-weight:bold;">
                    %.2f €
                  </span>
                </div>

                <!-- Footer -->
                <div style="padding:20px 30px; background:#F8F9FA; text-align:center;
                            border-top:1px solid #ECF0F1;">
                  <p style="margin:0; color:#95A5A6; font-size:12px;">
                    Smart Farm © 2026 — Merci de votre confiance 🌿
                  </p>
                </div>

              </div>
            </body>
            </html>
            """,
                stripeDisplay,
                lignes.toString(),
                commande.getTotalPrix()
        );
    }
}