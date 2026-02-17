package controllers;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceUser;

public class UpdateUser {

    @FXML private TextField tfNom;
    @FXML private TextField tfEmail;
    @FXML private ComboBox<String> cbRole;

    private User user;
    private final ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void initialize() {
        cbRole.getItems().addAll(
                "ADMIN",
                "USER",
                "EMPLOYER",
                "FOURNISSEUR",
                "VETERINAIRE"
        );
    }

    // appelé depuis DashboardController
    public void setUser(User user) {
        this.user = user;
        tfNom.setText(user.getUsername());
        tfEmail.setText(user.getEmail());
        cbRole.setValue(user.getRole());
    }


    @FXML
    void onSave() {

        if (tfNom.getText().isEmpty()
                || tfEmail.getText().isEmpty()
                || cbRole.getValue() == null) {

            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    "Tous les champs sont obligatoires");
            return;
        }

        try {
            user.setUsername(tfNom.getText());
            user.setEmail(tfEmail.getText());
            user.setRole(cbRole.getValue());

            serviceUser.modifier(user);

            showAlert(Alert.AlertType.INFORMATION,
                    "Succès",
                    "Utilisateur modifié avec succès");

            closeWindow();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    e.getMessage());
        }
    }

    @FXML
    void onCancel() {
        closeWindow();
    }


    @FXML
    public void saveChanges(ActionEvent actionEvent) {
        onSave();   // délégation propre
    }

    @FXML
    public void cancel(ActionEvent actionEvent) {
        onCancel(); // délégation propre
    }


    private void closeWindow() {
        Stage stage = (Stage) tfNom.getScene().getWindow();
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
