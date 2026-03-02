package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import entities.EmployeeTask;
import services.ServiceEmployeeTask;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class AfficherTasks implements Initializable {

    @FXML private TableView<EmployeeTask> tableTask;
    @FXML private TableColumn<EmployeeTask, String> colEmployeeName;
    @FXML private TableColumn<EmployeeTask, String> colDescription;
    @FXML private TableColumn<EmployeeTask, LocalDate> colDate;
    @FXML private TableColumn<EmployeeTask, Void> colRating;
    @FXML private TableColumn<EmployeeTask, Void> colActions;

    @FXML private Label lblTotalTasks;
    @FXML private Label lblTodayTasks;
    @FXML private Label lblAvgRating;
    @FXML private Label lblLastUpdate;
    @FXML private TextField txtSearch;

    private final ServiceEmployeeTask service = new ServiceEmployeeTask();
    private final ObservableList<EmployeeTask> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeDisplay"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("taskDescription"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("taskDate"));

        setupRatingColumn();
        setupActionsColumn();

        if (txtSearch != null) {
            txtSearch.textProperty().addListener(
                (obs, oldVal, newVal) -> filterTable(newVal)
            );
        }

        refreshTable();
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
                    ratingLabel.setStyle("-fx-text-fill: #e6edf3; -fx-font-size: 13px;");
                    setGraphic(ratingLabel);
                }
            }
        });
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnRate = new Button("⭐ Rate");

            {
                btnRate.setStyle(
                    "-fx-background-color: #ff9800; -fx-text-fill: white; " +
                    "-fx-font-size: 11px; -fx-padding: 6px 14px; " +
                    "-fx-background-radius: 8px; -fx-cursor: hand; " +
                    "-fx-font-weight: bold;"
                );
                
                btnRate.setOnAction(event -> {
                    EmployeeTask task = getTableView().getItems().get(getIndex());
                    showRatingDialog(task);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(btnRate);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private void showRatingDialog(EmployeeTask task) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Rate Task Performance");
        dialog.setHeaderText("Employee: " + task.getEmployeeName() + "\nTask: " + task.getTaskDescription());

        ButtonType submitButtonType = new ButtonType("Submit Rating", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        HBox ratingBox = new HBox(12);
        ratingBox.setAlignment(Pos.CENTER);
        ratingBox.setStyle("-fx-padding: 25px;");

        final int[] selectedRating = {task.getRating()};

        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            Button starBtn = new Button(i + " ⭐");
            starBtn.setPrefSize(70, 50);
            starBtn.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: bold; " +
                "-fx-background-radius: 10px; -fx-cursor: hand; " +
                (i == task.getRating() ? 
                    "-fx-background-color: #ff9800; -fx-text-fill: white;" : 
                    "-fx-background-color: #30363d; -fx-text-fill: #8b949e;")
            );
            
            starBtn.setOnAction(e -> {
                selectedRating[0] = rating;
                for (javafx.scene.Node node : ratingBox.getChildren()) {
                    if (node instanceof Button) {
                        Button btn = (Button) node;
                        btn.setStyle(
                            "-fx-font-size: 15px; -fx-font-weight: bold; " +
                            "-fx-background-radius: 10px; -fx-cursor: hand; " +
                            (btn == starBtn ? 
                                "-fx-background-color: #ff9800; -fx-text-fill: white;" : 
                                "-fx-background-color: #30363d; -fx-text-fill: #8b949e;")
                        );
                    }
                }
            });
            
            ratingBox.getChildren().add(starBtn);
        }

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(
            new Label("Select performance rating:"),
            ratingBox
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return selectedRating[0];
            }
            return null;
        });

        dialog.showAndWait().ifPresent(rating -> {
            try {
                service.updateRating(task.getId(), rating);
                task.setRating(rating);
                tableTask.refresh();
                updateStatistics();
                showAlert("Success", "Task rated with " + rating + " stars! ⭐");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to update rating: " + e.getMessage());
            }
        });
    }

    @FXML
    private void refreshTable() {
        data.clear();
        try {
            data.addAll(service.recuperer());
            tableTask.setItems(data);
            updateStatistics();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load tasks: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        if (lblTotalTasks != null)
            lblTotalTasks.setText(String.valueOf(data.size()));

        if (lblTodayTasks != null) {
            long todayCount = data.stream()
                .filter(t -> t.getTaskDate() != null &&
                             t.getTaskDate().equals(LocalDate.now()))
                .count();
            lblTodayTasks.setText(String.valueOf(todayCount));
        }

        if (lblAvgRating != null) {
            double avgRating = data.stream()
                .filter(t -> t.getRating() > 0)
                .mapToInt(EmployeeTask::getRating)
                .average()
                .orElse(0.0);
            lblAvgRating.setText(String.format("%.1f", avgRating));
        }

        if (lblLastUpdate != null)
            lblLastUpdate.setText("Just now");
    }

    private void filterTable(String search) {
        if (search == null || search.isEmpty()) {
            tableTask.setItems(data);
            return;
        }
        ObservableList<EmployeeTask> filtered = FXCollections.observableArrayList();
        String searchLower = search.toLowerCase();
        
        for (EmployeeTask t : data) {
            if (t.getTaskDescription().toLowerCase().contains(searchLower) ||
                (t.getEmployeeName() != null && t.getEmployeeName().toLowerCase().contains(searchLower)) ||
                (t.getEmployeePosition() != null && t.getEmployeePosition().toLowerCase().contains(searchLower)) ||
                String.valueOf(t.getEmployeeId()).contains(search)) {
                filtered.add(t);
            }
        }
        tableTask.setItems(filtered);
    }

    @FXML
    private void addNewTask() {
        URL fxmlUrl = getClass().getResource("/AjouterTask.fxml");
        if (fxmlUrl == null) {
            showAlert("Error", "AjouterTask.fxml not found!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Task");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open form: " + e.getMessage());
        }
    }

    @FXML
    private void editTask() {
        EmployeeTask selected = tableTask.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a task to edit");
            return;
        }
        URL fxmlUrl = getClass().getResource("/ModifierTask.fxml");
        if (fxmlUrl == null) {
            showAlert("Error", "ModifierTask.fxml not found!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            ModifierTask controller = loader.getController();
            controller.setTask(selected);
            Stage stage = new Stage();
            stage.setTitle("Edit Task");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open form: " + e.getMessage());
        }
    }

    @FXML
    private void deleteTask() {
        EmployeeTask selected = tableTask.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a task to delete");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Delete Task");
        confirm.setContentText("Delete this task?\n" + selected.getTaskDescription());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(selected);
                refreshTable();
                showAlert("Success", "Task deleted successfully!");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to delete: " + e.getMessage());
            }
        }
    }

    @FXML
    private void backToEmployees() {
        URL fxmlUrl = getClass().getResource("/AfficherEmploye.fxml");
        if (fxmlUrl == null) {
            showAlert("Error", "AfficherEmploye.fxml not found!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Stage stage = (Stage) tableTask.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Employee Management System");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Navigation failed");
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
