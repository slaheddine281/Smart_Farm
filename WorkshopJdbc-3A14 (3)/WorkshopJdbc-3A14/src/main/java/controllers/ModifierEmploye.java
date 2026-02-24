package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import entities.Employee;
import services.ServiceEmployee;

import java.sql.SQLException;

public class ModifierEmploye {

    @FXML private Label    lblEmployeeId;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtPhone;
    @FXML private ComboBox<String> cmbPosition;

    // Error labels
    @FXML private Label lblFirstNameError;
    @FXML private Label lblLastNameError;
    @FXML private Label lblPhoneError;
    @FXML private Label lblPositionError;

    private final ServiceEmployee service = new ServiceEmployee();
    private Employee currentEmployee;

    // ── Init ────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // ✅ Positions fixes
        cmbPosition.getItems().addAll(
                "Farmer",
                "Veterinarian",
                "Accountant"
        );

        // ✅ Validation temps réel — lettres seulement
        txtFirstName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("[a-zA-ZÀ-ÿ\\s]+")) {
                txtFirstName.setText(oldVal);
            }
            validateFirstName();
        });

        txtLastName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("[a-zA-ZÀ-ÿ\\s]+")) {
                txtLastName.setText(oldVal);
            }
            validateLastName();
        });

        // ✅ Validation téléphone — 8 chiffres max
        txtPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtPhone.setText(oldVal);
            } else if (newVal.length() > 8) {
                txtPhone.setText(oldVal);
            }
            validatePhone();
        });

        cmbPosition.valueProperty().addListener((obs, o, n) -> validatePosition());
    }

    // ── Populate form ────────────────────────────────────────
    public void setEmployee(Employee employee) {
        this.currentEmployee = employee;

        if (lblEmployeeId != null)
            lblEmployeeId.setText("#" + employee.getId());

        txtFirstName.setText(employee.getFirstName());
        txtLastName.setText(employee.getLastName());
        txtPhone.setText(employee.getPhone());

        // ✅ Sélectionner la position dans le ComboBox
        cmbPosition.setValue(employee.getPosition());
    }

    // ── Validators ───────────────────────────────────────────
    private boolean validateFirstName() {
        String val = txtFirstName.getText().trim();
        if (val.isEmpty()) {
            setError(txtFirstName, lblFirstNameError, "⚠ First name is required");
            return false;
        }
        setSuccess(txtFirstName, lblFirstNameError);
        return true;
    }

    private boolean validateLastName() {
        String val = txtLastName.getText().trim();
        if (val.isEmpty()) {
            setError(txtLastName, lblLastNameError, "⚠ Last name is required");
            return false;
        }
        setSuccess(txtLastName, lblLastNameError);
        return true;
    }

    private boolean validatePhone() {
        String val = txtPhone.getText().trim();
        if (val.isEmpty()) {
            setError(txtPhone, lblPhoneError, "⚠ Phone is required");
            return false;
        } else if (!val.matches("\\d{8}")) {
            setError(txtPhone, lblPhoneError, "⚠ Exactly 8 digits required");
            return false;
        }
        setSuccess(txtPhone, lblPhoneError);
        return true;
    }

    private boolean validatePosition() {
        if (cmbPosition.getValue() == null || cmbPosition.getValue().isEmpty()) {
            lblPositionError.setText("⚠ Please select a position");
            cmbPosition.setStyle("-fx-border-color: #f44336; -fx-border-width: 2px; " +
                    "-fx-border-radius: 8px;");
            return false;
        }
        lblPositionError.setText("");
        cmbPosition.setStyle("-fx-border-color: #00c853; -fx-border-width: 2px; " +
                "-fx-border-radius: 8px;");
        return true;
    }

    // ── Actions ──────────────────────────────────────────────
    @FXML
    private void updateEmployee() {
        // Valider tous les champs
        boolean ok = validateFirstName();
        ok &= validateLastName();
        ok &= validatePhone();
        ok &= validatePosition();

        if (!ok) return;

        // Mettre à jour l'objet
        currentEmployee.setFirstName(txtFirstName.getText().trim());
        currentEmployee.setLastName(txtLastName.getText().trim());
        currentEmployee.setPhone(txtPhone.getText().trim());
        currentEmployee.setPosition(cmbPosition.getValue());

        try {
            service.modifier(currentEmployee);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("✅ Success");
            alert.setHeaderText(null);
            alert.setContentText("Employee updated successfully!");
            alert.showAndWait();

            cancel();

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("❌ Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to update: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void resetForm() {
        if (currentEmployee != null) {
            setEmployee(currentEmployee);
            // Reset styles
            txtFirstName.setStyle("");
            txtLastName.setStyle("");
            txtPhone.setStyle("");
            cmbPosition.setStyle("");
            lblFirstNameError.setText("");
            lblLastNameError.setText("");
            lblPhoneError.setText("");
            lblPositionError.setText("");
        }
    }

    @FXML
    private void cancel() {
        ((Stage) txtFirstName.getScene().getWindow()).close();
    }

    // ── Visual helpers ───────────────────────────────────────
    private void setError(TextField field, Label errorLabel, String message) {
        errorLabel.setText(message);
        field.setStyle("-fx-border-color: #f44336; -fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-color: rgba(244,67,54,0.05);");
    }

    private void setSuccess(TextField field, Label errorLabel) {
        errorLabel.setText("");
        field.setStyle("-fx-border-color: #00c853; -fx-border-width: 2px; " +
                "-fx-border-radius: 8px;");
    }
}