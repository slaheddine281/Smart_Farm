package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import services.ServiceUser;
import utils.MyDatabase;

import java.sql.SQLException;

/**
 * 3-step forgot-password flow:
 * Step 1 — user enters their email → code sent
 * Step 2 — user enters the 6-digit code → verified
 * Step 3 — user enters a new password → saved
 */
public class ForgotPasswordController {

    /* ── Step 1 ── */
    @FXML
    private VBox step1Box;
    @FXML
    private TextField tfEmail;
    @FXML
    private Label lblStep1Status;

    /* ── Step 2 ── */
    @FXML
    private VBox step2Box;
    @FXML
    private TextField tfCode;
    @FXML
    private Label lblStep2Status;

    /* ── Step 3 ── */
    @FXML
    private VBox step3Box;
    @FXML
    private PasswordField tfNewPassword;
    @FXML
    private PasswordField tfConfirmPassword;
    @FXML
    private Label lblStep3Status;

    private final ServiceUser serviceUser = new ServiceUser(MyDatabase.getInstance().getConnection());

    /** Called by LoginController to pre-fill the email field. */
    public void prefillEmail(String email) {
        if (tfEmail != null) {
            tfEmail.setText(email);
        }
    }

    private String verifiedEmail; // stored after step 1

    /*
     * ════════════════════════════════════════
     * STEP 1 — Send code to email
     * ════════════════════════════════════════
     */
    @FXML
    void handleSendCode() {
        String email = tfEmail.getText().trim();

        if (email.isEmpty()) {
            setStatus(lblStep1Status, "⚠ Veuillez entrer votre adresse email.", false);
            return;
        }

        setStatus(lblStep1Status, "⏳ Envoi du code…", true);

        // Run in background so the UI doesn't freeze during SMTP
        new Thread(() -> {
            try {
                serviceUser.sendVerificationCode(email);
                verifiedEmail = email;
                javafx.application.Platform.runLater(() -> {
                    setStatus(lblStep1Status, "✅ Code envoyé à " + email, true);
                    goToStep(2);
                });
            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> setStatus(lblStep1Status, "❌ " + e.getMessage(), false));
            }
        }).start();
    }

    /*
     * ════════════════════════════════════════
     * STEP 2 — Verify code
     * ════════════════════════════════════════
     */
    @FXML
    void handleVerifyCode() {
        String code = tfCode.getText().trim();

        if (code.isEmpty()) {
            setStatus(lblStep2Status, "⚠ Veuillez entrer le code reçu.", false);
            return;
        }

        try {
            boolean valid = serviceUser.verifyEmailCode(verifiedEmail, code);
            if (valid) {
                setStatus(lblStep2Status, "✅ Code correct !", true);
                goToStep(3);
            } else {
                setStatus(lblStep2Status, "❌ Code incorrect ou expiré. Réessayez.", false);
            }
        } catch (SQLException e) {
            setStatus(lblStep2Status, "❌ Erreur SQL : " + e.getMessage(), false);
        }
    }

    /** Resend a fresh code (back to step 1 flow but keep email). */
    @FXML
    void handleResendCode() {
        tfCode.clear();
        goToStep(1);
        if (verifiedEmail != null) {
            tfEmail.setText(verifiedEmail);
        }
    }

    /*
     * ════════════════════════════════════════
     * STEP 3 — Set new password
     * ════════════════════════════════════════
     */
    @FXML
    void handleResetPassword() {
        String newPass = tfNewPassword.getText();
        String confirmPass = tfConfirmPassword.getText();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            setStatus(lblStep3Status, "⚠ Veuillez remplir les deux champs.", false);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            setStatus(lblStep3Status, "❌ Les mots de passe ne correspondent pas.", false);
            return;
        }

        if (newPass.length() < 6) {
            setStatus(lblStep3Status, "⚠ Le mot de passe doit contenir au moins 6 caractères.", false);
            return;
        }

        try {
            serviceUser.resetPassword(verifiedEmail, newPass);
            setStatus(lblStep3Status, "✅ Mot de passe modifié avec succès !", true);

            // Close window after short delay
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(
                    javafx.util.Duration.seconds(1.5));
            delay.setOnFinished(e -> closeWindow());
            delay.play();

        } catch (SQLException e) {
            setStatus(lblStep3Status, "❌ Erreur SQL : " + e.getMessage(), false);
        }
    }

    /* ── Helpers ───────────────────────────── */

    private void goToStep(int step) {
        step1Box.setVisible(step == 1);
        step1Box.setManaged(step == 1);
        step2Box.setVisible(step == 2);
        step2Box.setManaged(step == 2);
        step3Box.setVisible(step == 3);
        step3Box.setManaged(step == 3);
    }

    private void setStatus(Label lbl, String msg, boolean ok) {
        lbl.setText(msg);
        lbl.setStyle(ok
                ? "-fx-text-fill: #27ae60; -fx-font-size: 13px;"
                : "-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
    }

    private void closeWindow() {
        javafx.stage.Stage stage = (javafx.stage.Stage) tfEmail.getScene().getWindow();
        stage.close();
    }
}
