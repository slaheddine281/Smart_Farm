package controllers;

import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceUser;

import java.sql.SQLException;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private final ServiceUser serviceUser = new ServiceUser();

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
                showAlert("Erreur", "Identifiants invalides");
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage());
        }
    }

    // methode bch thel fenetre signup
    @FXML
    void openSignUpPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterUser.fxml"));
            Parent root = (Parent) loader.load(); // ✅ CAST OBLIGATOIRE en JDK 21 + JavaFX 21

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
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            var root = loader.load();
            var controller = loader.getController();
            ((DashboardController) controller).setUser(user);

            var stage = (Stage) txtEmail.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene((Parent) root, 1024, 600));
            stage.setTitle("Smart Farm — Tableau de bord");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        new Alert(Alert.AlertType.ERROR, msg)
                .showAndWait();
    }
}