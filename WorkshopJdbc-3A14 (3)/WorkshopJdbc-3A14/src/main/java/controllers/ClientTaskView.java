package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import entities.EmployeeTask;
import services.ServiceEmployeeTask;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class ClientTaskView implements Initializable {

    @FXML private Label lblTotalTasks;
    @FXML private Label lblTodayTasks;
    @FXML private Label lblWeekTasks;
    @FXML private Label lblCount;

    @FXML private TextField txtSearch;

    @FXML private TableView<EmployeeTask> tableTasks;
    @FXML private TableColumn<EmployeeTask, String> colEmployee;
    @FXML private TableColumn<EmployeeTask, String> colDescription;
    @FXML private TableColumn<EmployeeTask, LocalDate> colDate;

    private final ServiceEmployeeTask service = new ServiceEmployeeTask();
    private ObservableList<EmployeeTask> allTasks;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Table setup
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeDisplay"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("taskDescription"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("taskDate"));

        // Search listener
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterTasks());

        loadTasks();
    }

    private void loadTasks() {
        try {
            allTasks = FXCollections.observableArrayList(service.recuperer());
            displayTasks(allTasks);
            updateStatistics();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load tasks");
        }
    }

    private void updateStatistics() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        long todayCount = allTasks.stream()
            .filter(t -> t.getTaskDate() != null && t.getTaskDate().equals(today))
            .count();

        long weekCount = allTasks.stream()
            .filter(t -> t.getTaskDate() != null &&
                         !t.getTaskDate().isBefore(weekStart) &&
                         !t.getTaskDate().isAfter(weekEnd))
            .count();

        lblTotalTasks.setText(String.valueOf(allTasks.size()));
        lblTodayTasks.setText(String.valueOf(todayCount));
        lblWeekTasks.setText(String.valueOf(weekCount));
        lblCount.setText(allTasks.size() + " tasks");
    }

    private void filterTasks() {
        String search = txtSearch.getText().toLowerCase();

        if (search.isEmpty()) {
            displayTasks(allTasks);
            lblCount.setText(allTasks.size() + " tasks");
            return;
        }

        ObservableList<EmployeeTask> filtered = FXCollections.observableArrayList();
        
        for (EmployeeTask task : allTasks) {
            if (task.getTaskDescription().toLowerCase().contains(search) ||
                (task.getEmployeeName() != null && 
                 task.getEmployeeName().toLowerCase().contains(search)) ||
                (task.getEmployeePosition() != null && 
                 task.getEmployeePosition().toLowerCase().contains(search))) {
                filtered.add(task);
            }
        }

        displayTasks(filtered);
        lblCount.setText(filtered.size() + " tasks");
    }

    private void displayTasks(ObservableList<EmployeeTask> tasks) {
        tableTasks.setItems(tasks);
    }

    @FXML
    private void backToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableTasks.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Smart Farm Portal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
