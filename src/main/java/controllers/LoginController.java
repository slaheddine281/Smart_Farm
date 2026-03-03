package controllers;

import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceUser;
import utils.MyDatabase;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class LoginController {

    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button btnForgotPassword;
    @FXML
    private Label lblError;

    private final ServiceUser serviceUser = new ServiceUser(MyDatabase.getInstance().getConnection());

    @FXML
    public void initialize() {
        // Nothing to initialize here
    }

    @FXML
    void handleLogin() {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Email et mot de passe requis");
            return;
        }

        try {
            User user = serviceUser.getUserByEmailAndPassword(email, password);
            if (user != null) {
                openDashboard(user);
            } else {
                if (isValidEmail(email)) {
                    btnForgotPassword.setVisible(true);
                }
                showAlert("Erreur", "Identifiants invalides");
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void togglePasswordVisibility() {
        // Password toggle is not active in the current FXML layout
    }

    @FXML
    void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ForgotPassword.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Réinitialisation du mot de passe");
            stage.setResizable(false);
            // Pre-fill email if already typed
            String email = txtEmail.getText().trim();
            if (!email.isEmpty()) {
                ForgotPasswordController ctrl = loader.getController();
                // We expose a helper to pre-set the email field
                ctrl.prefillEmail(email);
            }
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre : " + e.getMessage());
        }
    }

    @FXML
    void openSignUpPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterUser.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer un compte");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire.");
        }
    }

    private void openDashboard(User user) {
        try {
            var loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            var root = loader.load();
            var controller = loader.getController();
            ((DashboardController) controller).setUser(user);

            var stage = (Stage) txtEmail.getScene().getWindow();
            stage.setScene(new Scene((Parent) root, 1024, 600));
            stage.setTitle("Smart Farm — Tableau de bord");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.matches(regex, email);
    }

    private void showAlert(String title, String msg) {
        new Alert(Alert.AlertType.ERROR, msg)
                .showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}