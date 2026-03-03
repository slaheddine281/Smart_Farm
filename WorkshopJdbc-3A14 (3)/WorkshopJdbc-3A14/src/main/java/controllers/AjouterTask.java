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
import java.util.logging.Level;
import java.util.logging.Logger;

public class AjouterTask implements Initializable {

    // ─────────────────────────────────────────────────────────────
    // ✅ CHAMPS FXML
    // ─────────────────────────────────────────────────────────────
    @FXML private ComboBox<Employee> cmbEmployee;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> cmbEventType;

    // ─────────────────────────────────────────────────────────────
    // ✅ SERVICES
    // ─────────────────────────────────────────────────────────────
    private final ServiceEmployee employeeService = new ServiceEmployee();
    private final ServiceEmployeeTask taskService = new ServiceEmployeeTask();
    private static final Logger LOGGER = Logger.getLogger(AjouterTask.class.getName());

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadEmployees();
        setupEventTypeComboBox();
        datePicker.setValue(LocalDate.now());
        setupDateValidation();
    }

    // ─────────────────────────────────────────────────────────────
    private void setupEventTypeComboBox() {
        cmbEventType.getItems().addAll(
                "🐾 Soin Animal",
                "🏥 Traitement Vétérinaire",
                "🌾 Tâche Agricole",
                "👷 Shift Employé",
                "📋 Général"
        );
        cmbEventType.setValue("📋 Général");
        cmbEventType.setTooltip(new Tooltip("Sélectionnez le type d'événement pour Google Calendar"));
    }

    // ─────────────────────────────────────────────────────────────
    private void setupDateValidation() {
        datePicker.setDayCellFactory(new Callback<>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        LocalDate today = LocalDate.now();
                        if (date.isBefore(today)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #30363d; -fx-text-fill: #6e7681;");
                        }
                    }
                };
            }
        });

        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && newDate.isBefore(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Invalid Date",
                        "You cannot select a date in the past!\nPlease select today or a future date.");
                datePicker.setValue(LocalDate.now());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    private void loadEmployees() {
        try {
            List<Employee> employees = employeeService.recuperer();
            cmbEmployee.getItems().addAll(employees);

            cmbEmployee.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Employee emp, boolean empty) {
                    super.updateItem(emp, empty);
                    if (empty || emp == null) {
                        setText(null);
                    } else {
                        setText(emp.getFirstName() + " " + emp.getLastName() + " (" + emp.getPosition() + ")");
                    }
                }
            });

            cmbEmployee.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Employee emp, boolean empty) {
                    super.updateItem(emp, empty);
                    if (empty || emp == null) {
                        setText("Select an employee...");
                    } else {
                        setText(emp.getFirstName() + " " + emp.getLastName() + " (" + emp.getPosition() + ")");
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load employees: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    @FXML
    private void addTask() {
        // Validation employé
        if (cmbEmployee.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select an employee!");
            return;
        }

        // Validation description
        String description = txtDescription.getText().trim();
        if (description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a task description!");
            return;
        }
        if (description.length() < 5) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Task description must be at least 5 characters long!");
            return;
        }

        // Validation date
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a date!");
            return;
        }
        if (selectedDate.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date",
                    "Cannot create a task with a past date!\nSelected: " + selectedDate + "\nToday: " + LocalDate.now());
            return;
        }

        try {
            Employee selectedEmployee = cmbEmployee.getValue();
            EmployeeTask task = new EmployeeTask(selectedEmployee.getId(), description, selectedDate);
            task.setEmployeeName(selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName());
            task.setEmployeePosition(selectedEmployee.getPosition());
            task.setRating(0);

            String eventType = getEventTypeKey(cmbEventType.getValue());

            // ✅ TOUJOURS ENVOYER À VOTRE NUMÉRO POUR LES TESTS
            String employeePhone = "+21650093975";  // ← VOTRE numéro WhatsApp vérifié
            String employeeName = selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName();

            // ✅ APPEL À LA MÉTHODE AVEC WHATSAPP + CALENDAR
            taskService.ajouterAvecCalendarEtWhatsApp(
                    task,
                    eventType,
                    employeePhone,    // Toujours votre numéro pour tester
                    employeeName      // Nom complet de l'employé
            );

            // ✅ MESSAGE DE SUCCÈS MIS À JOUR
            String calendarMsg = taskService.isCalendarAvailable()
                    ? "📅 Événement créé dans Google Calendar !"
                    : "⚠️ Calendar indisponible";

            String whatsappMsg = taskService.isWhatsAppAvailable()
                    ? "📱 Notification WhatsApp envoyée !"
                    : "⚠️ WhatsApp indisponible";

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Task added successfully!\n\n" +
                            "👤 Employee: " + employeeName + "\n" +
                            "📝 Task: " + description + "\n" +
                            "📅 Date: " + selectedDate + "\n" +
                            "🔖 Type: " + cmbEventType.getValue() + "\n\n" +
                            calendarMsg + "\n" +
                            whatsappMsg);

            LOGGER.info("✅ Tâche ajoutée : " + task.getTaskDescription() + " pour " + employeeName);
            clearForm();
            closeWindow();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur lors de l'ajout : " + e.getMessage(), e);
            String msg = e.getMessage() != null ? e.getMessage() : "An unexpected error occurred";
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add task:\n" + msg);
        }
    }

    // ─────────────────────────────────────────────────────────────
    private String getEventTypeKey(String displayValue) {
        if (displayValue == null) return "general";
        return switch (displayValue) {
            case "🐾 Soin Animal" -> "soin_animal";
            case "🏥 Traitement Vétérinaire" -> "traitement_veterinaire";
            case "🌾 Tâche Agricole" -> "tache_agricole";
            case "👷 Shift Employé" -> "shift_employe";
            default -> "general";
        };
    }

    // ─────────────────────────────────────────────────────────────
    @FXML
    private void clearForm() {
        cmbEmployee.setValue(null);
        txtDescription.clear();
        datePicker.setValue(LocalDate.now());
        cmbEventType.setValue("📋 Général");
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
        alert.initOwner(cmbEmployee.getScene().getWindow());
        alert.showAndWait();
    }
}