package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import entities.Employee;
import services.ServiceEmployee;

import java.io.IOException;
import java.sql.SQLException;

public class AjouterEmploye {

    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cmbPosition;

    // Error labels
    @FXML private Label lblFirstNameError;
    @FXML private Label lblLastNameError;
    @FXML private Label lblPhoneError;
    @FXML private Label lblPositionError;

    private ServiceEmployee service = new ServiceEmployee();

    @FXML
    public void initialize() {
        // ✅ Positions: Farmer / Veterinarian / Accountant
        cmbPosition.getItems().addAll(
                "Farmer",
                "Veterinarian",
                "Accountant"
        );

        // ✅ Real-time validation: First Name — letters only
        txtFirstName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("[a-zA-ZÀ-ÿ\\s]+")) {
                txtFirstName.setText(oldVal); // Bloquer la saisie
            }
            validateFirstName();
        });

        // ✅ Real-time validation: Last Name — letters only
        txtLastName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("[a-zA-ZÀ-ÿ\\s]+")) {
                txtLastName.setText(oldVal); // Bloquer la saisie
            }
            validateLastName();
        });

        // ✅ Real-time validation: Phone — 8 digits max
        txtPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            // Accepter seulement les chiffres, max 8
            if (!newVal.matches("\\d*")) {
                txtPhone.setText(oldVal); // Bloquer les lettres
            } else if (newVal.length() > 8) {
                txtPhone.setText(oldVal); // Bloquer après 8 chiffres
            }
            validatePhone();
        });

        // ✅ Real-time validation: Position
        cmbPosition.valueProperty().addListener((obs, oldVal, newVal) -> validatePosition());
    }

    // ── Validators ──────────────────────────────────────────

    private boolean validateFirstName() {
        String val = txtFirstName.getText().trim();
        if (val.isEmpty()) {
            setError(txtFirstName, lblFirstNameError, "⚠ First name is required");
            return false;
        } else if (!val.matches("[a-zA-ZÀ-ÿ\\s]+")) {
            setError(txtFirstName, lblFirstNameError, "⚠ Letters only");
            return false;
        } else {
            setSuccess(txtFirstName, lblFirstNameError);
            return true;
        }
    }

    private boolean validateLastName() {
        String val = txtLastName.getText().trim();
        if (val.isEmpty()) {
            setError(txtLastName, lblLastNameError, "⚠ Last name is required");
            return false;
        } else if (!val.matches("[a-zA-ZÀ-ÿ\\s]+")) {
            setError(txtLastName, lblLastNameError, "⚠ Letters only");
            return false;
        } else {
            setSuccess(txtLastName, lblLastNameError);
            return true;
        }
    }

    private boolean validatePhone() {
        String val = txtPhone.getText().trim();
        if (val.isEmpty()) {
            setError(txtPhone, lblPhoneError, "⚠ Phone number is required");
            return false;
        } else if (!val.matches("\\d{8}")) {
            setError(txtPhone, lblPhoneError, "⚠ Exactly 8 digits required (e.g. 22334455)");
            return false;
        } else {
            setSuccess(txtPhone, lblPhoneError);
            return true;
        }
    }

    private boolean validatePosition() {
        if (cmbPosition.getValue() == null || cmbPosition.getValue().isEmpty()) {
            lblPositionError.setText("⚠ Please select a position");
            cmbPosition.setStyle("-fx-border-color: #f44336; -fx-border-width: 2px; " +
                    "-fx-border-radius: 8px; -fx-background-radius: 8px;");
            return false;
        } else {
            lblPositionError.setText("");
            cmbPosition.setStyle("-fx-border-color: #00c853; -fx-border-width: 2px; " +
                    "-fx-border-radius: 8px; -fx-background-radius: 8px;");
            return true;
        }
    }

    // ── Visual feedback helpers ──────────────────────────────

    private void setError(TextField field, Label errorLabel, String message) {
        errorLabel.setText(message);
        field.setStyle("-fx-border-color: #f44336; -fx-border-width: 2px; " +
                "-fx-border-radius: 8px; -fx-background-color: rgba(244,67,54,0.05);");
    }

    private void setSuccess(TextField field, Label errorLabel) {
        errorLabel.setText("");
        field.setStyle("-fx-border-color: #00c853; -fx-border-width: 2px; " +
                "-fx-border-radius: 8px;");
    }

    private void resetStyle(TextField field, Label errorLabel) {
        errorLabel.setText("");
        field.setStyle("");
    }

    // ── Actions ─────────────────────────────────────────────

    @FXML
    private void addEmployee() {
        // Valider tous les champs
        boolean ok = validateFirstName();
        ok &= validateLastName();
        ok &= validatePhone();
        ok &= validatePosition();

        // ❌ Si un champ est invalide → arrêter
        if (!ok) {
            return;
        }

        // ✅ Tous les champs sont valides → ajouter
        Employee employee = new Employee();
        employee.setFirstName(txtFirstName.getText().trim());
        employee.setLastName(txtLastName.getText().trim());
        employee.setPhone(txtPhone.getText().trim());
        employee.setPosition(cmbPosition.getValue());

        try {
            service.ajouter(employee);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("✅ Success");
            alert.setHeaderText(null);
            alert.setContentText("Employee added successfully!");
            alert.showAndWait();

            clearForm();

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("❌ Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to add employee: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void clearForm() {
        txtFirstName.clear();
        txtLastName.clear();
        txtPhone.clear();
        txtEmail.clear();
        cmbPosition.getSelectionModel().clearSelection();

        // Reset all styles and errors
        resetStyle(txtFirstName, lblFirstNameError);
        resetStyle(txtLastName, lblLastNameError);
        resetStyle(txtPhone, lblPhoneError);
        lblPositionError.setText("");
        cmbPosition.setStyle("");
    }

    @FXML
    private void showList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEmploye.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtFirstName.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}