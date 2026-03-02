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
        System.out.println("===========================================");
        System.out.println("✅ ClientDashboard initialization started");
        System.out.println("===========================================");
        
        // Table setup
        colTaskEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeDisplay"));
        colTaskDescription.setCellValueFactory(new PropertyValueFactory<>("taskDescription"));
        colTaskDate.setCellValueFactory(new PropertyValueFactory<>("taskDate"));

        loadDashboardData();
        
        System.out.println("✅ ClientDashboard ready!");
    }

    private void loadDashboardData() {
        try {
            System.out.println("📊 Loading employees...");
            
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
            
            System.out.println("✅ Employees: " + employees.size() + 
                             " (Farmers: " + farmers + 
                             ", Vets: " + vets + 
                             ", Accountants: " + accountants + ")");

            // Update charts
            updateBarChart(farmers, vets, accountants);
            updatePieChart(farmers, vets, accountants);
            
            System.out.println("✅ Charts updated");

            // Load recent tasks
            System.out.println("📋 Loading tasks...");
            ObservableList<EmployeeTask> tasks = FXCollections.observableArrayList(
                taskService.recuperer()
            );
            
            int limit = Math.min(5, tasks.size());
            ObservableList<EmployeeTask> recentTasks = FXCollections.observableArrayList(
                tasks.subList(0, limit)
            );
            tableRecentTasks.setItems(recentTasks);
            
            System.out.println("✅ Tasks loaded: " + recentTasks.size());

        } catch (SQLException e) {
            System.err.println("❌ Database error:");
            e.printStackTrace();
            showAlert("Error", "Failed to load data: " + e.getMessage());
        }
    }

    private void updateBarChart(long farmers, long vets, long accountants) {
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Employees");
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
        System.out.println("🔄 Opening employee list...");
        try {
            URL fxmlUrl = getClass().getResource("/ClientEmployeeList.fxml");
            if (fxmlUrl == null) {
                System.err.println("❌ ClientEmployeeList.fxml not found!");
                showAlert("Error", "Employee list view not found!");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Stage stage = (Stage) lblTotalEmployees.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Employee Directory");
            System.out.println("✅ Navigation successful");
            
        } catch (IOException e) {
            System.err.println("❌ Navigation error:");
            e.printStackTrace();
            showAlert("Error", "Failed to open employee list: " + e.getMessage());
        }
    }

    @FXML
    private void viewTasks() {
        System.out.println("🔄 Opening task view...");
        try {
            URL fxmlUrl = getClass().getResource("/ClientTaskView.fxml");
            if (fxmlUrl == null) {
                System.err.println("❌ ClientTaskView.fxml not found!");
                showAlert("Error", "Task view not found!");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Stage stage = (Stage) lblTotalEmployees.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Task Overview");
            System.out.println("✅ Navigation successful");
            
        } catch (IOException e) {
            System.err.println("❌ Navigation error:");
            e.printStackTrace();
            showAlert("Error", "Failed to open task view: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
