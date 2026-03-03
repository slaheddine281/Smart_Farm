package controllers;

import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import services.ServiceUser;
import utils.MyDatabase;

import java.sql.SQLException;
import java.util.List;
import javafx.scene.control.Label;
public class DashboardController {

    @FXML private ListView<User> userList;
    @FXML private Label lblWelcome;
    @FXML private Label lblMessage;

    private User currentUser;
    private User selectedUser;

    private final ServiceUser serviceUser = new ServiceUser(MyDatabase.getInstance().getConnection());

    @FXML
    public void initialize() {

        // tahsin affichage
        userList.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(
                             " Nom: " + user.getUsername()
                            + " | Email: " + user.getEmail()
                            + " | Rôle: " + user.getRole());
                }
            }
        });

        userList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldValue, newValue) -> selectedUser = newValue
        );
    }

    public void setUser(User user) {
        this.currentUser = user;
        lblWelcome.setText("Bienvenue, " + user.getUsername() + " (" + user.getRole() + ")");
        configurePermissions(user.getRole());
    }

    private void configurePermissions(String role) {

        switch (role.toUpperCase()) {

            case "ADMIN":
            case "USER":
                chargerUtilisateurs();
                break;

            case "EMPLOYER":
            case "FOURNISSEUR":
            case "VETERINAIRE":
                chargerCurrentUserOnly();
                break;

            default:
                lblMessage.setText("Accès non autorisé.");
                userList.setVisible(false);
        }
    }

    private void chargerUtilisateurs() {
        try {
            List<User> users = serviceUser.recuperer();
            userList.getItems().clear();
            userList.getItems().addAll(users);
        } catch (SQLException e) {
            lblMessage.setText("❌ Erreur : " + e.getMessage());
        }
    }

    private void chargerCurrentUserOnly() {
        userList.getItems().clear();
        userList.getItems().add(currentUser);
    }

    @FXML
    void openAddUserPage() {

        if (!("ADMIN".equals(currentUser.getRole()) ||
                "USER".equals(currentUser.getRole()))) {

            lblMessage.setText("⚠ Seuls ADMIN et USER peuvent ajouter.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterUser.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter utilisateur");
            stage.show();

        } catch (Exception e) {
            lblMessage.setText("Erreur ouverture AjouterUser.");
            e.printStackTrace();
        }
    }

    @FXML
    void editSelectedUser() {

        if (selectedUser == null) {
            lblMessage.setText("⚠ Veuillez sélectionner un utilisateur.");
            return;
        }

        if (!("ADMIN".equals(currentUser.getRole()) ||
                "USER".equals(currentUser.getRole())) &&
                currentUser.getId() != selectedUser.getId()) {

            lblMessage.setText("⚠ Vous pouvez modifier seulement votre compte.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/update.fxml"));
            Parent root = loader.load();

            UpdateUser controller = loader.getController();
            controller.setUser(selectedUser);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier utilisateur");
            stage.show();

        } catch (Exception e) {
            lblMessage.setText("Erreur ouverture UpdateUser.");
            e.printStackTrace();
        }
    }

    @FXML
    void deleteSelectedUser() {

        if (selectedUser == null) {
            lblMessage.setText("⚠ Veuillez sélectionner un utilisateur.");
            return;
        }

        if (!("ADMIN".equals(currentUser.getRole()) ||
                "USER".equals(currentUser.getRole()))) {

            lblMessage.setText("⚠ Seuls ADMIN et USER peuvent supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setContentText("Supprimer : "
                + selectedUser.getUsername()
                + " (" + selectedUser.getRole() + ") ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceUser.supprimer(selectedUser.getId());
                    chargerUtilisateurs();
                    lblMessage.setText("✅ Utilisateur supprimé !");
                } catch (SQLException e) {
                    lblMessage.setText("❌ Erreur : " + e.getMessage());
                }
            }
        });
    }

    @FXML

    void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();

            // Utilisez userList au lieu de lblWelcome
            Stage stage = (Stage) userList.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void openChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chatbot.fxml"));
            Parent root = loader.load(); // ✅ Maintenant valide

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Smart Farm — Assistant");
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'ouvrir le chatbot :\n" + e.getMessage());
            alert.showAndWait();
        }
    }
}
