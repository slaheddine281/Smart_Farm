package controllers;

import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import services.ServiceUser;
import utils.MyDatabase;

import java.sql.SQLException;
import java.util.List;

public class AfficherUser {

    @FXML
    private ListView<String> userList;

    private final ServiceUser serviceUser = new ServiceUser(MyDatabase.getInstance().getConnection());

    @FXML
    public void initialize() {
        chargerUtilisateurs();
    }

    public void chargerUtilisateurs() {
        try {
            List<User> users = serviceUser.recuperer();
            userList.getItems().clear();
            for (User u : users) {
                // ✅ ID masqué ici
                userList.getItems().add(
                        String.format("Nom: %s | Email: %s | Rôle: %s",
                                u.getUsername(), u.getEmail(), u.getRole()));
            }
        } catch (SQLException e) {
            userList.getItems().add("❌ Erreur : " + e.getMessage());
        }
    }

    @FXML
    void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userList.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}