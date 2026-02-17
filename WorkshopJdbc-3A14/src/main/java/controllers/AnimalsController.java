package controllers;

import entities.Animals;
import services.Serviceanimals;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AnimalsController {

    @FXML private TableView<Animals>              animalsTable;
    @FXML private TableColumn<Animals, Integer>   colId;
    @FXML private TableColumn<Animals, String>    colType;
    @FXML private TableColumn<Animals, String>    colBreed;
    @FXML private TableColumn<Animals, LocalDate> colBirthDate;
    @FXML private TableColumn<Animals, String>    colHealthStatus;
    @FXML private TableColumn<Animals, Void>      colActions;
    @FXML private TextField                        searchField;
    @FXML private ComboBox<String>                 filterComboBox;
    @FXML private Label                            lblTotalAnimals;

    private Serviceanimals              service;
    private ObservableList<Animals>     animalsList;

    @FXML
    public void initialize() {
        service     = new Serviceanimals();
        animalsList = FXCollections.observableArrayList();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colBreed.setCellValueFactory(new PropertyValueFactory<>("breed"));
        colBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        colHealthStatus.setCellValueFactory(new PropertyValueFactory<>("healthStatus"));

        setupActionsColumn();

        filterComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "Vache", "Chèvre", "Mouton", "Poulet"
        ));
        filterComboBox.setValue("Tous");

        loadAnimals();
    }

    // ══════════════════════════════════════════════
    //  ✅ NAVIGATION → Vue Visiteur
    // ══════════════════════════════════════════════

    @FXML
    private void handleGoToVisiteur() {
        navigateTo("/fxml/VisiteurProductionsView.fxml");
    }

    /**
     * Remonte jusqu'au StackPane contentArea du MainController
     * et charge la vue cible — exactement comme MainController.loadView().
     */
    private void navigateTo(String fxmlPath) {
        try {
            // Cherche le StackPane parent (contentArea) dans la hiérarchie de scène
            StackPane contentArea = (StackPane) animalsTable
                    .getScene()
                    .lookup("#contentArea");

            if (contentArea == null) {
                System.out.println("❌ contentArea introuvable dans la scène.");
                return;
            }

            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.out.println("❌ FXML introuvable : " + fxmlPath);
                return;
            }

            Parent view = FXMLLoader.load(resource);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            System.out.println("✅ Navigation vers " + fxmlPath);

        } catch (Exception e) {
            System.out.println("❌ Erreur navigation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════
    //  COLONNE ACTIONS
    // ══════════════════════════════════════════════

    private void setupActionsColumn() {
        Callback<TableColumn<Animals, Void>, TableCell<Animals, Void>> cellFactory =
                param -> new TableCell<>() {
                    private final Button btnEdit   = new Button("✏️ Modifier");
                    private final Button btnDelete = new Button("🗑️ Supprimer");
                    private final HBox   buttons   = new HBox(5, btnEdit, btnDelete);
                    {
                        btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-background-radius: 5;");
                        btnDelete.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 5;");
                        buttons.setAlignment(Pos.CENTER);
                        btnEdit.setOnAction(e   -> handleEdit(getTableView().getItems().get(getIndex())));
                        btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                    }
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : buttons);
                    }
                };
        colActions.setCellFactory(cellFactory);
    }

    // ══════════════════════════════════════════════
    //  CRUD
    // ══════════════════════════════════════════════

    private void loadAnimals() {
        try {
            List<Animals> animals = service.recuperer();
            animalsList.clear();
            animalsList.addAll(animals);
            animalsTable.setItems(animalsList);
            lblTotalAnimals.setText(String.valueOf(animals.size()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        Dialog<Animals> dialog = createAnimalDialog("Nouvel Animal", null);
        Optional<Animals> result = dialog.showAndWait();
        result.ifPresent(animal -> {
            try {
                service.ajouter(animal);
                loadAnimals();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "✅ Animal ajouté !");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        });
    }

    private void handleEdit(Animals animal) {
        Dialog<Animals> dialog = createAnimalDialog("Modifier Animal", animal);
        Optional<Animals> result = dialog.showAndWait();
        result.ifPresent(modifiedAnimal -> {
            try {
                service.modifier(modifiedAnimal);
                loadAnimals();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "✅ Animal modifié !");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        });
    }

    private void handleDelete(Animals animal) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'animal #" + animal.getId());
        confirm.setContentText("Type: " + animal.getType() + "\nRace: " + animal.getBreed());
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(animal);
                loadAnimals();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "✅ Animal supprimé !");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadAnimals();
    }

    @FXML
    private void handleSearch() {
        String search = searchField.getText().toLowerCase();
        if (search.isEmpty()) {
            animalsTable.setItems(animalsList);
        } else {
            ObservableList<Animals> filtered = animalsList.filtered(
                    a -> a.getType().toLowerCase().contains(search) ||
                            a.getBreed().toLowerCase().contains(search)
            );
            animalsTable.setItems(filtered);
        }
    }

    @FXML
    private void handleFilter() {
        String filter = filterComboBox.getValue();
        if ("Tous".equals(filter)) {
            animalsTable.setItems(animalsList);
        } else {
            ObservableList<Animals> filtered = animalsList.filtered(
                    a -> a.getType().equals(filter)
            );
            animalsTable.setItems(filtered);
        }
    }

    // ══════════════════════════════════════════════
    //  DIALOG ANIMAL
    // ══════════════════════════════════════════════

    private Dialog<Animals> createAnimalDialog(String title, Animals animal) {
        Dialog<Animals> dialog = new Dialog<>();
        dialog.setTitle(title);

        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<String> typeField = new ComboBox<>(FXCollections.observableArrayList(
                "Vache", "Chèvre", "Mouton", "Poulet", "Cheval"
        ));
        TextField breedField = new TextField();
        DatePicker birthDatePicker = new DatePicker();
        ComboBox<String> healthField = new ComboBox<>(FXCollections.observableArrayList(
                "Bonne santé", "Excellente", "Sous surveillance", "Sous traitement", "Malade"
        ));

        Label errType   = makeErrLabel();
        Label errBreed  = makeErrLabel();
        Label errDate   = makeErrLabel();
        Label errHealth = makeErrLabel();

        if (animal != null) {
            typeField.setValue(animal.getType());
            breedField.setText(animal.getBreed());
            birthDatePicker.setValue(animal.getBirthDate());
            healthField.setValue(animal.getHealthStatus());
        }

        grid.add(new Label("Type *:"),           0, 0); grid.add(typeField,        1, 0); grid.add(errType,   2, 0);
        grid.add(new Label("Race *:"),            0, 1); grid.add(breedField,       1, 1); grid.add(errBreed,  2, 1);
        grid.add(new Label("Date naissance *:"),  0, 2); grid.add(birthDatePicker,  1, 2); grid.add(errDate,   2, 2);
        grid.add(new Label("Santé *:"),           0, 3); grid.add(healthField,      1, 3); grid.add(errHealth, 2, 3);

        dialog.getDialogPane().setContent(grid);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveButton);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            boolean valid = true;
            if (typeField.getValue() == null)                                          { errType.setText("⚠ Sélectionne un type !");      valid = false; } else errType.setText("");
            if (breedField.getText().trim().isEmpty())                                 { errBreed.setText("⚠ La race est obligatoire !");  valid = false; }
            else if (breedField.getText().trim().length() < 2)                         { errBreed.setText("⚠ Minimum 2 caractères !");     valid = false; }
            else if (!breedField.getText().matches("[a-zA-ZÀ-ÿ\\s-]+"))               { errBreed.setText("⚠ Lettres uniquement !");        valid = false; }
            else                                                                        errBreed.setText("");
            if (birthDatePicker.getValue() == null)                                    { errDate.setText("⚠ La date est obligatoire !");   valid = false; }
            else if (birthDatePicker.getValue().isAfter(LocalDate.now()))              { errDate.setText("⚠ Date future non autorisée !"); valid = false; }
            else                                                                        errDate.setText("");
            if (healthField.getValue() == null)                                        { errHealth.setText("⚠ Sélectionne un statut !");   valid = false; } else errHealth.setText("");
            if (!valid) event.consume();
        });

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                if (animal != null) {
                    animal.setType(typeField.getValue());
                    animal.setBreed(breedField.getText().trim());
                    animal.setBirthDate(birthDatePicker.getValue());
                    animal.setHealthStatus(healthField.getValue());
                    return animal;
                } else {
                    return new Animals(
                            typeField.getValue(),
                            breedField.getText().trim(),
                            birthDatePicker.getValue(),
                            healthField.getValue()
                    );
                }
            }
            return null;
        });

        return dialog;
    }

    private Label makeErrLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        return l;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}