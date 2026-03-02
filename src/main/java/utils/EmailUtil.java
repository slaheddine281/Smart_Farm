package utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Utility class for sending emails via Gmail SMTP.
 *
 * ⚠️ SETUP REQUIRED:
 * 1. Replace SENDER_EMAIL with your Gmail address.
 * 2. Replace SENDER_PASSWORD with a Gmail App Password
 * (Google Account → Security → 2-Step Verification → App passwords).
 * Do NOT use your regular Gmail password here.
 */
public class EmailUtil {

    // ✏️ Configure these two values with your Gmail credentials
    private static final String SENDER_EMAIL = "ayadislah86@gmail.com";
    private static final String SENDER_PASSWORD = "shgi irab ydfz wwmi";

    /**
     * Sends a 6-digit verification code to the given recipient email.
     *
     * @param toEmail the recipient's email address
     * @param code    the 6-digit code to include in the message
     * @throws MessagingException if the email could not be sent
     */
    public static void sendVerificationCode(String toEmail, String code) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("🌾 Smart Farm — Code de vérification");
        message.setText(
                "Bonjour,\n\n" +
                        "Votre code de vérification Smart Farm est :\n\n" +
                        "        " + code + "\n\n" +
                        "Ce code expire dès que vous l'utilisez.\n" +
                        "Si vous n'avez pas demandé ce code, ignorez ce message.\n\n" +
                        "— L'équipe Smart Farm");

        Transport.send(message);
    }
}
