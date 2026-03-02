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
import java.util.ResourceBundle;

public class ClientTaskView implements Initializable {

    @FXML private Label lblTotalTasks;
    @FXML private Label lblTodayTasks;
    @FXML private Label lblWeekTasks;
    @FXML private Label lblAvgRating;
    @FXML private Label lblCount;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbRatingFilter;

    @FXML private TableView<EmployeeTask> tableTasks;
    @FXML private TableColumn<EmployeeTask, String> colEmployee;
    @FXML private TableColumn<EmployeeTask, String> colDescription;
    @FXML private TableColumn<EmployeeTask, LocalDate> colDate;
    @FXML private TableColumn<EmployeeTask, Void> colRating;

    private final ServiceEmployeeTask service = new ServiceEmployeeTask();
    private ObservableList<EmployeeTask> allTasks;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Table setup
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeDisplay"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("taskDescription"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("taskDate"));

        // Setup rating column (READ ONLY - no button)
        setupRatingColumn();

        // Rating filter setup
        cmbRatingFilter.getItems().addAll(
                "All ratings",
                "5 stars",
                "4+ stars",
                "3+ stars",
                "Not rated"
        );
        cmbRatingFilter.setValue("All ratings");
        cmbRatingFilter.setOnAction(e -> filterTasks());

        // Search listener
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterTasks());

        loadTasks();
    }

    private void setupRatingColumn() {
        colRating.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    EmployeeTask task = getTableView().getItems().get(getIndex());
                    Label ratingLabel = new Label(task.getRatingStars());
                    ratingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
                    setGraphic(ratingLabel);
                }
            }
        });
    }

    private void loadTasks() {
        try {
            allTasks = FXCollections.observableArrayList(service.recuperer());
            displayTasks(allTasks);
            updateStatistics();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load tasks");
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

        double avgRating = allTasks.stream()
                .filter(t -> t.getRating() > 0)
                .mapToInt(EmployeeTask::getRating)
                .average()
                .orElse(0.0);

        lblTotalTasks.setText(String.valueOf(allTasks.size()));
        lblTodayTasks.setText(String.valueOf(todayCount));
        lblWeekTasks.setText(String.valueOf(weekCount));
        lblAvgRating.setText(String.format("%.1f", avgRating));
        lblCount.setText(allTasks.size() + " tasks");
    }

    private void filterTasks() {
        String search = txtSearch.getText().toLowerCase();
        String ratingFilter = cmbRatingFilter.getValue();

        ObservableList<EmployeeTask> filtered = FXCollections.observableArrayList();

        for (EmployeeTask task : allTasks) {
            // Search filter
            boolean matchesSearch = search.isEmpty() ||
                    task.getTaskDescription().toLowerCase().contains(search) ||
                    (task.getEmployeeName() != null &&
                            task.getEmployeeName().toLowerCase().contains(search)) ||
                    (task.getEmployeePosition() != null &&
                            task.getEmployeePosition().toLowerCase().contains(search));

            // Rating filter
            boolean matchesRating = true;
            if (ratingFilter != null && !ratingFilter.equals("All ratings")) {
                if (ratingFilter.equals("5 stars")) {
                    matchesRating = task.getRating() == 5;
                } else if (ratingFilter.equals("4+ stars")) {
                    matchesRating = task.getRating() >= 4;
                } else if (ratingFilter.equals("3+ stars")) {
                    matchesRating = task.getRating() >= 3;
                } else if (ratingFilter.equals("Not rated")) {
                    matchesRating = task.getRating() == 0;
                }
            }

            if (matchesSearch && matchesRating) {
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}