package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import entities.Employee;
import entities.EmployeeTask;
import services.ServiceEmployee;
import services.ServiceEmployeeTask;

import java.sql.SQLException;
import java.util.List;

public class ModifierTask {

    @FXML private Label lblTaskId;
    @FXML private ComboBox<String> cmbEmployee;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker dpTaskDate;

    private ServiceEmployeeTask service = new ServiceEmployeeTask();
    private ServiceEmployee serviceEmployee = new ServiceEmployee();
    private EmployeeTask currentTask;
    private List<Employee> employees;

    @FXML
    public void initialize() {
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
    }

    public void setTask(EmployeeTask task) {
        this.currentTask = task;

        if (lblTaskId != null)
            lblTaskId.setText("#" + task.getId());

        // Sélectionner l'employé correspondant
        for (String item : cmbEmployee.getItems()) {
            if (item.startsWith(task.getEmployeeId() + " - ")) {
                cmbEmployee.setValue(item);
                break;
            }
        }

        txtDescription.setText(task.getTaskDescription());
        dpTaskDate.setValue(task.getTaskDate());
    }

    @FXML
    private void saveTask() {
        if (cmbEmployee.getValue() == null ||
                txtDescription.getText().trim().isEmpty() ||
                dpTaskDate.getValue() == null) {
            showAlert("⚠ Validation", "Please fill all required fields");
            return;
        }

        int employeeId = Integer.parseInt(cmbEmployee.getValue().split(" - ")[0]);

        currentTask.setEmployeeId(employeeId);
        currentTask.setTaskDescription(txtDescription.getText().trim());
        currentTask.setTaskDate(dpTaskDate.getValue());

        try {
            service.modifier(currentTask);
            showAlert("✅ Success", "Task updated successfully!");
            cancel();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("❌ Error", "Failed to update task: " + e.getMessage());
        }
    }

    @FXML
    private void resetForm() {
        if (currentTask != null) setTask(currentTask);
    }

    @FXML
    private void cancel() {
        ((Stage) txtDescription.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}