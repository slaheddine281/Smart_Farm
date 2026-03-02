package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import entities.Employee;
import entities.EmployeeTask;
import services.ServiceEmployee;
import services.ServiceEmployeeTask;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AjouterTask implements Initializable {

    @FXML private ComboBox<Employee> cmbEmployee;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker datePicker;

    private final ServiceEmployee employeeService = new ServiceEmployee();
    private final ServiceEmployeeTask taskService = new ServiceEmployeeTask();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadEmployees();
        
        // ✅ Définir la date par défaut à aujourd'hui
        datePicker.setValue(LocalDate.now());
        
        // ✅ BLOQUER LES DATES PASSÉES
        setupDateValidation();
    }

    private void setupDateValidation() {
        // Désactiver visuellement les dates passées dans le calendrier
        datePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        
                        // Désactiver toutes les dates avant aujourd'hui
                        LocalDate today = LocalDate.now();
                        if (date.isBefore(today)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #30363d; -fx-text-fill: #6e7681;");
                        }
                    }
                };
            }
        });

        // ✅ Validation supplémentaire au changement de valeur
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && newDate.isBefore(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Invalid Date", 
                    "You cannot select a date in the past!\nPlease select today or a future date.");
                datePicker.setValue(LocalDate.now());
            }
        });
    }

    private void loadEmployees() {
        try {
            List<Employee> employees = employeeService.recuperer();
            cmbEmployee.getItems().addAll(employees);
            
            // Afficher "Nom Prénom (Position)"
            cmbEmployee.setCellFactory(param -> new ListCell<Employee>() {
                @Override
                protected void updateItem(Employee emp, boolean empty) {
                    super.updateItem(emp, empty);
                    if (empty || emp == null) {
                        setText(null);
                    } else {
                        setText(emp.getFirstName() + " " + emp.getLastName() + 
                               " (" + emp.getPosition() + ")");
                    }
                }
            });
            
            cmbEmployee.setButtonCell(new ListCell<Employee>() {
                @Override
                protected void updateItem(Employee emp, boolean empty) {
                    super.updateItem(emp, empty);
                    if (empty || emp == null) {
                        setText("Select an employee...");
                    } else {
                        setText(emp.getFirstName() + " " + emp.getLastName() + 
                               " (" + emp.getPosition() + ")");
                    }
                }
            });
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                "Failed to load employees: " + e.getMessage());
        }
    }

    @FXML
    private void addTask() {
        // Validation de l'employé
        if (cmbEmployee.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", 
                "Please select an employee!");
            return;
        }

        // Validation de la description
        String description = txtDescription.getText().trim();
        if (description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", 
                "Please enter a task description!");
            return;
        }
        
        if (description.length() < 5) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", 
                "Task description must be at least 5 characters long!");
            return;
        }

        // Validation de la date
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", 
                "Please select a date!");
            return;
        }

        // ✅ VALIDATION FINALE : Vérifier que la date n'est pas dans le passé
        if (selectedDate.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date", 
                "Cannot create a task with a past date!\n" +
                "Selected: " + selectedDate + "\n" +
                "Today: " + LocalDate.now());
            return;
        }

        // Créer la tâche
        try {
            Employee selectedEmployee = cmbEmployee.getValue();
            EmployeeTask task = new EmployeeTask(
                selectedEmployee.getId(),
                description,
                selectedDate
            );
            task.setRating(0); // Rating par défaut = 0 (non noté)
            
            taskService.ajouter(task);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                "Task added successfully!\n\n" +
                "Employee: " + selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName() + "\n" +
                "Date: " + selectedDate);
            
            clearForm();
            closeWindow();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                "Failed to add task: " + e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        cmbEmployee.setValue(null);
        txtDescription.clear();
        datePicker.setValue(LocalDate.now()); // Reset à aujourd'hui
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cmbEmployee.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
