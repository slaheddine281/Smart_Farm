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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import entities.Employee;
import services.ExportService;
import services.ServiceEmployee;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class StatisticsController implements Initializable {

    @FXML private Label lblTotalEmployees;
    @FXML private Label lblFarmers;
    @FXML private Label lblVeterinarians;
    @FXML private Label lblAccountants;

    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;

    @FXML private TableView<Employee> tableEmployees;
    @FXML private TableColumn<Employee, Integer> colId;
    @FXML private TableColumn<Employee, String> colFirstName;
    @FXML private TableColumn<Employee, String> colLastName;
    @FXML private TableColumn<Employee, String> colPhone;
    @FXML private TableColumn<Employee, String> colPosition;

    private final ServiceEmployee service = new ServiceEmployee();
    private ObservableList<Employee> data;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));

        refreshData();
    }

    @FXML
    private void refreshData() {
        try {
            data = FXCollections.observableArrayList(service.recuperer());

            long farmers = data.stream()
                .filter(e -> "Farmer".equalsIgnoreCase(e.getPosition()))
                .count();
            long vets = data.stream()
                .filter(e -> "Veterinarian".equalsIgnoreCase(e.getPosition()))
                .count();
            long accountants = data.stream()
                .filter(e -> "Accountant".equalsIgnoreCase(e.getPosition()))
                .count();

            lblTotalEmployees.setText(String.valueOf(data.size()));
            lblFarmers.setText(String.valueOf(farmers));
            lblVeterinarians.setText(String.valueOf(vets));
            lblAccountants.setText(String.valueOf(accountants));

            updateBarChart(farmers, vets, accountants);
            updatePieChart(farmers, vets, accountants);
            tableEmployees.setItems(data);

        } catch (SQLException e) {
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
            pieChart.getData().add(new PieChart.Data("Farmer (" + farmers + ")", farmers));
        if (vets > 0)
            pieChart.getData().add(new PieChart.Data("Veterinarian (" + vets + ")", vets));
        if (accountants > 0)
            pieChart.getData().add(new PieChart.Data("Accountant (" + accountants + ")", accountants));
    }

    @FXML
    private void exportReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Excel Report");
            fileChooser.setInitialFileName(ExportService.generateFileName());
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );

            File file = fileChooser.showSaveDialog(tableEmployees.getScene().getWindow());
            if (file != null) {
                ExportService.exportToExcel(new ArrayList<>(data), file.getAbsolutePath());
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("✅ Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("Report exported successfully!\n\n📁 " + file.getAbsolutePath());
                alert.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Export failed: " + e.getMessage());
        }
    }

    @FXML
    private void backToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEmploye.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableEmployees.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Employee Management System");
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
