package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import entities.Employee;
import entities.EmployeeTask;
import services.ServiceEmployee;
import services.ServiceEmployeeTask;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ClientDashboard implements Initializable {

    @FXML private Label lblTotalEmployees;
    @FXML private Label lblFarmers;
    @FXML private Label lblVeterinarians;
    @FXML private Label lblAccountants;

    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;

    @FXML private TableView<EmployeeTask> tableRecentTasks;
    @FXML private TableColumn<EmployeeTask, String> colTaskEmployee;
    @FXML private TableColumn<EmployeeTask, String> colTaskDescription;
    @FXML private TableColumn<EmployeeTask, LocalDate> colTaskDate;

    private final ServiceEmployee employeeService = new ServiceEmployee();
    private final ServiceEmployeeTask taskService = new ServiceEmployeeTask();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Table setup
        colTaskEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeDisplay"));
        colTaskDescription.setCellValueFactory(new PropertyValueFactory<>("taskDescription"));
        colTaskDate.setCellValueFactory(new PropertyValueFactory<>("taskDate"));

        loadDashboardData();
    }

    private void loadDashboardData() {
        try {
            // Load employees
            ObservableList<Employee> employees = FXCollections.observableArrayList(
                employeeService.recuperer()
            );

            long farmers = employees.stream()
                .filter(e -> "Farmer".equalsIgnoreCase(e.getPosition())).count();
            long vets = employees.stream()
                .filter(e -> "Veterinarian".equalsIgnoreCase(e.getPosition())).count();
            long accountants = employees.stream()
                .filter(e -> "Accountant".equalsIgnoreCase(e.getPosition())).count();

            // Update KPIs
            lblTotalEmployees.setText(String.valueOf(employees.size()));
            lblFarmers.setText(String.valueOf(farmers));
            lblVeterinarians.setText(String.valueOf(vets));
            lblAccountants.setText(String.valueOf(accountants));

            // Update charts
            updateBarChart(farmers, vets, accountants);
            updatePieChart(farmers, vets, accountants);

            // Load recent tasks (limit to 5)
            ObservableList<EmployeeTask> tasks = FXCollections.observableArrayList(
                taskService.recuperer()
            );
            ObservableList<EmployeeTask> recentTasks = FXCollections.observableArrayList(
                tasks.subList(0, Math.min(5, tasks.size()))
            );
            tableRecentTasks.setItems(recentTasks);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load dashboard data");
        }
    }

    private void updateBarChart(long farmers, long vets, long accountants) {
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Farmer", farmers));
        series.getData().add(new XYChart.Data<>("Veterinarian", vets));
        series.getData().add(new XYChart.Data<>("Accountant", accountants));
        barChart.getData().add(series);
    }

    private void updatePieChart(long farmers, long vets, long accountants) {
        pieChart.getData().clear();
        if (farmers > 0)
            pieChart.getData().add(new PieChart.Data("Farmers (" + farmers + ")", farmers));
        if (vets > 0)
            pieChart.getData().add(new PieChart.Data("Veterinarians (" + vets + ")", vets));
        if (accountants > 0)
            pieChart.getData().add(new PieChart.Data("Accountants (" + accountants + ")", accountants));
    }

    @FXML
    private void viewEmployees() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientEmployeeList.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblTotalEmployees.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Employee Directory");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open employee list");
        }
    }

    @FXML
    private void viewTasks() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientTaskView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblTotalEmployees.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Task Overview");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open task view");
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
