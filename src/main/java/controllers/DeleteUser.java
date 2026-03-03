package controllers;

import entities.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceUser;
import utils.MyDatabase;

import java.sql.SQLException;

public class DeleteUser {

    @FXML private Label lblNom;
    @FXML private Label lblEmail;
    @FXML private Label lblRole;

    private User user;
    private final ServiceUser serviceUser = new ServiceUser(MyDatabase.getInstance().getConnection());

    public void setUser(User user) {
        this.user = user;
        lblNom.setText(user.getUsername());
        lblEmail.setText(user.getEmail());
        lblRole.setText(user.getRole());
    }

    @FXML
    void onDelete() {
        try {
            // règle métier
            if ("ADMIN".equals(user.getRole())) {
                showAlert(Alert.AlertType.ERROR,
                        "Suppression interdite",
                        "Impossible de supprimer un administrateur");
                return;
            }

            serviceUser.supprimer(user.getId());

            showAlert(Alert.AlertType.INFORMATION,
                    "Succès",
                    "Utilisateur supprimé avec succès");

            closeWindow();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    e.getMessage());
        }
    }

    @FXML
    void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) lblNom.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
