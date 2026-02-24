package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import entities.Employee;
import entities.EmployeeTask;
import services.ServiceEmployee;
import services.ServiceEmployeeTask;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AjouterTask {

    @FXML private ComboBox<String> cmbEmployee;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker dpTaskDate;

    @FXML private Label lblEmployeeError;
    @FXML private Label lblDescriptionError;
    @FXML private Label lblDateError;

    private ServiceEmployeeTask service = new ServiceEmployeeTask();
    private ServiceEmployee serviceEmployee = new ServiceEmployee();
    private List<Employee> employees;

    @FXML
    public void initialize() {
        // Charger les employés dans le ComboBox
        try {
            employees = serviceEmployee.recuperer();
            for (Employee emp : employees) {
                cmbEmployee.getItems().add(
                        emp.getId() + " - " + emp.getFirstName() + " " + emp.getLastName()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Date par défaut = aujourd'hui
        dpTaskDate.setValue(LocalDate.now());

        // Validation en temps réel
        txtDescription.textProperty().addListener((obs, o, n) -> validateDescription());
        dpTaskDate.valueProperty().addListener((obs, o, n) -> validateDate());
        cmbEmployee.valueProperty().addListener((obs, o, n) -> validateEmployee());
    }

    private boolean validateEmployee() {
        if (cmbEmployee.getValue() == null) {
            lblEmployeeError.setText("⚠ Please select an employee");
            cmbEmployee.setStyle("-fx-border-color: #f44336; -fx-border-width: 2px; -fx-border-radius: 8px;");
            return false;
        }
        lblEmployeeError.setText("");
        cmbEmployee.setStyle("-fx-border-color: #00c853; -fx-border-width: 2px; -fx-border-radius: 8px;");
        return true;
    }

    private boolean validateDescription() {
        String val = txtDescription.getText().trim();
        if (val.isEmpty()) {
            lblDescriptionError.setText("⚠ Task description is required");
            txtDescription.setStyle("-fx-border-color: #f44336; -fx-border-width: 2px; -fx-border-radius: 8px;");
            return false;
        } else if (val.length() < 5) {
            lblDescriptionError.setText("⚠ Description too short (min 5 chars)");
            txtDescription.setStyle("-fx-border-color: #f44336; -fx-border-width: 2px; -fx-border-radius: 8px;");
            return false;
        }
        lblDescriptionError.setText("");
        txtDescription.setStyle("-fx-border-color: #00c853; -fx-border-width: 2px; -fx-border-radius: 8px;");
        return true;
    }

    private boolean validateDate() {
        if (dpTaskDate.getValue() == null) {
            lblDateError.setText("⚠ Please select a date");
            return false;
        }
        lblDateError.setText("");
        return true;
    }

    @FXML
    private void addTask() {
        boolean ok = validateEmployee();
        ok &= validateDescription();
        ok &= validateDate();

        if (!ok) return;

        // Récupérer l'ID de l'employé sélectionné
        String selected = cmbEmployee.getValue();
        int employeeId = Integer.parseInt(selected.split(" - ")[0]);

        EmployeeTask task = new EmployeeTask(
                employeeId,
                txtDescription.getText().trim(),
                dpTaskDate.getValue()
        );

        try {
            service.ajouter(task);
            showSuccess("Task added successfully! ✅");
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to add task: " + e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        cmbEmployee.getSelectionModel().clearSelection();
        txtDescription.clear();
        dpTaskDate.setValue(LocalDate.now());
        lblEmployeeError.setText("");
        lblDescriptionError.setText("");
        lblDateError.setText("");
        cmbEmployee.setStyle("");
        txtDescription.setStyle("");
    }

    @FXML
    private void cancel() {
        ((Stage) txtDescription.getScene().getWindow()).close();
    }

    private void showSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("✅ Success");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("❌ Error");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}