package controllers;

import entities.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import services.ServiceUser;

import java.io.File;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class AjouterUser {

    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ComboBox<String> comboRole;
    @FXML private ImageView imageView;

    private final ServiceUser serviceUser = new ServiceUser();
    private String photoProfessionelle = "";

    @FXML
    public void initialize() {
        comboRole.getItems().addAll(
                "USER",
                "ADMIN",
                "EMPLOYER",
                "FOURNISSEUR",
                "VETERINAIRE"
        );
    }

    @FXML
    void choisirPhoto() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            photoProfessionelle = file.toURI().toString();
            imageView.setImage(new Image(photoProfessionelle));
        }
    }

    @FXML
    void addUser() {

        String username = txtUsername.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String confirm = txtConfirmPassword.getText();
        String role = comboRole.getValue();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()
                || confirm.isEmpty() || role == null) {

            showAlert(Alert.AlertType.ERROR,  "Tous les champs sont obligatoires");
            return;
        }

        if (!password.equals(confirm)) {
            showAlert(Alert.AlertType.ERROR, "Les mots de passe ne correspondent pas");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Email invalide");
            return;
        }

        User user = new User(username, email, password, role, photoProfessionelle);

        try {
            serviceUser.ajouter(user);
            ouvrirAfficherUser();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    private void ouvrirAfficherUser() {
        try {
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(getClass().getResource("/AfficherUser.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage =
                    (javafx.stage.Stage) txtUsername.getScene().getWindow();

            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Liste des utilisateurs");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.matches(regex, email);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
