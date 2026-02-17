package controllers;

import entities.User;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import services.ServiceUser;

import java.sql.SQLException;
import java.util.List;

public class AfficherUser {

    @FXML
    private ListView<String> userList; // ← Liste de String

    private final ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void initialize() {
        chargerUtilisateurs();
    }

    public void chargerUtilisateurs() {
        try {
            List<User> users = serviceUser.recuperer();
            userList.getItems().clear();
            for (User u : users) {
                // Convertir chaque User en String (via toString() ou format personnalisé)
                userList.getItems().add(
                        String.format("ID: %d | Nom: %s | Email: %s | Rôle: %s",
                                u.getId(), u.getUsername(), u.getEmail(), u.getRole())
                );
            }
        } catch (SQLException e) {
            userList.getItems().add("❌ Erreur : " + e.getMessage());
        }
    }
}