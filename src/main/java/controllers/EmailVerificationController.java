package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import services.ServiceUser;
import utils.MyDatabase;

import java.sql.SQLException;

public class EmailVerificationController {

    @FXML
    private Label lblEmail;
    @FXML
    private Label lblStatus;
    @FXML
    private TextField tfCode;

    private String email;
    private final ServiceUser serviceUser = new ServiceUser(MyDatabase.getInstance().getConnection());

    /**
     * Called by the parent controller to pre-fill the email and trigger sending the
     * code.
     */
    public void setEmail(String email) {
        this.email = email;
        lblEmail.setText(email);
        trySendCode();
    }

    private void trySendCode() {
        try {
            serviceUser.sendVerificationCode(email);
            lblStatus.setStyle("-fx-text-fill: #28a745;");
            lblStatus.setText("✅ Code envoyé à " + email);
        } catch (SQLException e) {
            lblStatus.setStyle("-fx-text-fill: #dc3545;");
            lblStatus.setText("❌ Erreur : " + e.getMessage());
        }
    }

    @FXML
    void handleVerifyCode() {
        String code = tfCode.getText().trim();

        if (code.isEmpty()) {
            lblStatus.setStyle("-fx-text-fill: #dc3545;");
            lblStatus.setText("⚠ Veuillez entrer le code.");
            return;
        }

        try {
            boolean valid = serviceUser.verifyEmailCode(email, code);
            if (valid) {
                lblStatus.setStyle("-fx-text-fill: #28a745;");
                lblStatus.setText("✅ Email vérifié avec succès !");
            } else {
                lblStatus.setStyle("-fx-text-fill: #dc3545;");
                lblStatus.setText("❌ Code invalide ou expiré.");
            }
        } catch (SQLException e) {
            lblStatus.setStyle("-fx-text-fill: #dc3545;");
            lblStatus.setText("❌ Erreur SQL : " + e.getMessage());
        }
    }

    @FXML
    void handleResendCode() {
        if (email == null || email.isEmpty()) {
            lblStatus.setStyle("-fx-text-fill: #dc3545;");
            lblStatus.setText("❌ Aucun email défini.");
            return;
        }
        trySendCode();
    }
}
