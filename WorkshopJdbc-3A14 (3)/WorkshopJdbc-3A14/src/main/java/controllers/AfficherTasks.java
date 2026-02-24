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
import java.util.Optional;
import java.util.ResourceBundle;

public class AfficherTasks implements Initializable {

    @FXML private TableView<EmployeeTask>              tableTask;
    @FXML private TableColumn<EmployeeTask, String>    colEmployeeName;    // ✅ CHANGÉ - String au lieu de Integer
    @FXML private TableColumn<EmployeeTask, String>    colDescription;
    @FXML private TableColumn<EmployeeTask, LocalDate> colDate;

    @FXML private Label     lblTotalTasks;
    @FXML private Label     lblTodayTasks;
    @FXML private Label     lblLastUpdate;
    @FXML private TextField txtSearch;

    private final ServiceEmployeeTask service = new ServiceEmployeeTask();
    private final ObservableList<EmployeeTask> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // ✅ CHANGÉ - colEmployeeName utilise maintenant getEmployeeDisplay()
        colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeDisplay"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("taskDescription"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("taskDate"));

        if (txtSearch != null) {
            txtSearch.textProperty().addListener(
                (obs, oldVal, newVal) -> filterTable(newVal)
            );
        }

        refreshTable();
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
            showAlert("Erreur", "Impossible de charger les tâches : " + e.getMessage());
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

        if (lblLastUpdate != null)
            lblLastUpdate.setText("Just now");
    }

    // ✅ AMÉLIORÉ - Recherche par nom, position et description
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
            showAlert("Erreur",
                "Fichier AjouterTask.fxml introuvable !\n" +
                "Verifiez qu'il est dans src/main/resources/");
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
            showAlert("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void editTask() {
        EmployeeTask selected = tableTask.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez selectionner une tache a modifier");
            return;
        }
        URL fxmlUrl = getClass().getResource("/ModifierTask.fxml");
        if (fxmlUrl == null) {
            showAlert("Erreur",
                "Fichier ModifierTask.fxml introuvable !\n" +
                "Verifiez qu'il est dans src/main/resources/");
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
            showAlert("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void deleteTask() {
        EmployeeTask selected = tableTask.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez selectionner une tache a supprimer");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Delete Task");
        confirm.setContentText("Supprimer cette tache ?\n" + selected.getTaskDescription());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(selected);
                refreshTable();
                showAlert("Succes", "Tache supprimee avec succes !");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer : " + e.getMessage());
            }
        }
    }

    @FXML
    private void backToEmployees() {
        URL fxmlUrl = getClass().getResource("/AfficherEmploye.fxml");
        if (fxmlUrl == null) {
            showAlert("Erreur", "Fichier AfficherEmploye.fxml introuvable !");
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
            showAlert("Erreur", "Impossible de retourner a la liste");
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
