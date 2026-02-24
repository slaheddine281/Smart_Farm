package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import entities.Employee;
import services.ServiceEmployee;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ClientEmployeeList implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterPosition;
    @FXML private FlowPane employeeGrid;
    @FXML private Label lblCount;

    private final ServiceEmployee service = new ServiceEmployee();
    private ObservableList<Employee> allEmployees;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup filters
        cmbFilterPosition.getItems().addAll("All Positions", "Farmer", "Veterinarian", "Accountant");
        cmbFilterPosition.setValue("All Positions");
        cmbFilterPosition.setOnAction(e -> filterEmployees());

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterEmployees());

        loadEmployees();
    }

    private void loadEmployees() {
        try {
            allEmployees = FXCollections.observableArrayList(service.recuperer());
            displayEmployees(allEmployees);
            lblCount.setText(allEmployees.size() + " employees");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load employees");
        }
    }

    private void filterEmployees() {
        String search = txtSearch.getText().toLowerCase();
        String position = cmbFilterPosition.getValue();

        ObservableList<Employee> filtered = FXCollections.observableArrayList();
        
        for (Employee emp : allEmployees) {
            boolean matchesSearch = search.isEmpty() ||
                emp.getFirstName().toLowerCase().contains(search) ||
                emp.getLastName().toLowerCase().contains(search) ||
                emp.getPhone().contains(search);

            boolean matchesPosition = position.equals("All Positions") ||
                emp.getPosition().equalsIgnoreCase(position);

            if (matchesSearch && matchesPosition) {
                filtered.add(emp);
            }
        }

        displayEmployees(filtered);
        lblCount.setText(filtered.size() + " employees");
    }

    private void displayEmployees(ObservableList<Employee> employees) {
        employeeGrid.getChildren().clear();

        for (Employee emp : employees) {
            VBox card = createEmployeeCard(emp);
            employeeGrid.getChildren().add(card);
        }
    }

    private VBox createEmployeeCard(Employee emp) {
        VBox card = new VBox(12);
        card.getStyleClass().add("employee-card");
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(340);
        card.setMinHeight(150);

        // Name
        Label name = new Label(emp.getFirstName() + " " + emp.getLastName());
        name.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Position badge
        Label position = new Label(emp.getPosition());
        position.setStyle(getPositionStyle(emp.getPosition()));
        position.setPadding(new Insets(6, 14, 6, 14));

        // Phone
        Label phone = new Label("📞 " + emp.getPhone());
        phone.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");

        // ID
        Label id = new Label("ID: #" + emp.getId());
        id.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");

        card.getChildren().addAll(name, position, phone, id);
        return card;
    }

    private String getPositionStyle(String position) {
        switch (position.toLowerCase()) {
            case "farmer":
                return "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; " +
                       "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 12px;";
            case "veterinarian":
                return "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; " +
                       "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 12px;";
            case "accountant":
                return "-fx-background-color: #f3e5f5; -fx-text-fill: #6a1b9a; " +
                       "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 12px;";
            default:
                return "-fx-background-color: #f5f5f5; -fx-text-fill: #616161; " +
                       "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 12px;";
        }
    }

    @FXML
    private void backToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) employeeGrid.getScene().getWindow();
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
