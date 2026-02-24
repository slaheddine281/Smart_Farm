package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

public class AfficherEmploye implements Initializable {

    @FXML private TableView<Employee> tableEmployes;
    @FXML private TableColumn<Employee, Integer> colId;
    @FXML private TableColumn<Employee, String>  colFirstName;
    @FXML private TableColumn<Employee, String>  colLastName;
    @FXML private TableColumn<Employee, String>  colPhone;
    @FXML private TableColumn<Employee, String>  colPosition;

    @FXML private Label         lblTotalEmployees;
    @FXML private Label         lblActivePositions;
    @FXML private Label         lblLastUpdate;
    @FXML private TextField     txtSearch;
    @FXML private ComboBox<String> cmbFilterPosition;

    private final ServiceEmployee service = new ServiceEmployee();
    private final ObservableList<Employee> data = FXCollections.observableArrayList();

    // ── Init ────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));

        // ComboBox — positions fixes
        if (cmbFilterPosition != null) {
            cmbFilterPosition.getItems().addAll(
                    "All Positions", "Farmer", "Veterinarian", "Accountant"
            );
            cmbFilterPosition.getSelectionModel().selectFirst();
            cmbFilterPosition.setOnAction(e -> filterByPosition());
        }

        // Recherche temps réel
        if (txtSearch != null) {
            txtSearch.textProperty().addListener(
                    (obs, oldVal, newVal) -> filterTable(newVal)
            );
        }

        refreshTable();
    }

    // ── CRUD ────────────────────────────────────────────────
    @FXML
    private void refreshTable() {
        data.clear();
        try {
            data.addAll(service.recuperer());
            tableEmployes.setItems(data);
            updateStatistics();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données : " + e.getMessage());
        }
    }

    @FXML
    private void addNewEmployee() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterEmploye.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un employé");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout");
        }
    }

    @FXML
    private void updateEmployee() {
        Employee selected = tableEmployes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un employé à modifier");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEmploye.fxml"));
            Parent root = loader.load();

            ModifierEmploye controller = loader.getController();
            controller.setEmployee(selected);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'employé");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification");
        }
    }

    @FXML
    private void deleteEmployee() {
        Employee selected = tableEmployes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un employé à supprimer");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'employé");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer " +
                selected.getFirstName() + " " + selected.getLastName() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(selected);
                refreshTable();
                showAlert("Succès", "L'employé a été supprimé avec succès");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer : " + e.getMessage());
            }
        }
    }

    // ── Navigation ──────────────────────────────────────────
    @FXML
    private void openTasks() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherTasks.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableEmployes.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Employee Tasks Management");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la gestion des tâches");
        }
    }

    @FXML
    private void closeWindow(ActionEvent event) {
        ((Stage) tableEmployes.getScene().getWindow()).close();
    }

    // ── Filtres ─────────────────────────────────────────────
    private void filterTable(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            tableEmployes.setItems(data);
            return;
        }
        ObservableList<Employee> filtered = FXCollections.observableArrayList();
        for (Employee emp : data) {
            if (emp.getFirstName().toLowerCase().contains(searchText.toLowerCase()) ||
                    emp.getLastName().toLowerCase().contains(searchText.toLowerCase())  ||
                    emp.getPhone().contains(searchText)                                  ||
                    emp.getPosition().toLowerCase().contains(searchText.toLowerCase())) {
                filtered.add(emp);
            }
        }
        tableEmployes.setItems(filtered);
    }

    private void filterByPosition() {
        String selected = cmbFilterPosition.getValue();
        if (selected == null || selected.equals("All Positions")) {
            tableEmployes.setItems(data);
            return;
        }
        ObservableList<Employee> filtered = FXCollections.observableArrayList();
        for (Employee emp : data) {
            if (emp.getPosition().equalsIgnoreCase(selected)) {
                filtered.add(emp);
            }
        }
        tableEmployes.setItems(filtered);
    }

    // ── Statistiques ────────────────────────────────────────
    private void updateStatistics() {
        if (lblTotalEmployees != null)
            lblTotalEmployees.setText(String.valueOf(data.size()));

        if (lblActivePositions != null) {
            Set<String> unique = new HashSet<>();
            for (Employee emp : data) unique.add(emp.getPosition());
            lblActivePositions.setText(String.valueOf(unique.size()));
        }

        if (lblLastUpdate != null)
            lblLastUpdate.setText("Just now");
    }

    // ✅ NOUVELLE MÉTHODE — Ouvre la page Statistics complète
    @FXML
    private void showStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StatisticsView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableEmployes.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Workforce Statistics");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open statistics: " + e.getMessage());
        }
    }

    private long countByPosition(String position) {
        return data.stream()
                .filter(e -> e.getPosition().equalsIgnoreCase(position))
                .count();
    }

    // ✅ NOUVELLE MÉTHODE — Export Excel avec dialogue
    @FXML
    private void exportData() {
        try {
            // Ouvrir un dialogue pour choisir où sauvegarder
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Excel File");
            fileChooser.setInitialFileName(ExportService.generateFileName());
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );

            File file = fileChooser.showSaveDialog(tableEmployes.getScene().getWindow());

            if (file != null) {
                ExportService.exportToExcel(new ArrayList<>(data), file.getAbsolutePath());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("✅ Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("File exported successfully!\n\n📁 Location:\n" + file.getAbsolutePath());
                alert.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("❌ Export Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to export file:\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    // ── Helper ──────────────────────────────────────────────
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}