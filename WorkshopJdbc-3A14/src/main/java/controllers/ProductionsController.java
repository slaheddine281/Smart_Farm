package controllers;

import entities.AnimalProduction;
import entities.Animals;
import services.ServiceAnimalProduction;
import services.Serviceanimals;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class ProductionsController {

    @FXML private TableView<AnimalProduction>          productionsTable;
    @FXML private TableColumn<AnimalProduction, Integer>   colId;
    @FXML private TableColumn<AnimalProduction, Integer>   colAnimalId;
    @FXML private TableColumn<AnimalProduction, String>    colType;
    @FXML private TableColumn<AnimalProduction, Double>    colQuantity;
    @FXML private TableColumn<AnimalProduction, LocalDate> colDate;
    @FXML private TableColumn<AnimalProduction, Void>      colActions;
    @FXML private TextField                                 searchField;
    @FXML private ComboBox<String>                          filterComboBox;
    @FXML private Label                                     lblTotal;
    @FXML private Label                                     lblTotalLait;
    @FXML private Label                                     lblTotalOeufs;
    @FXML private Label                                     lblMoyenne;

    private ServiceAnimalProduction          service;
    private Serviceanimals                   serviceAnimals;
    private ObservableList<AnimalProduction> productionsList;

    // Règles métier
    private static final Map<String, List<String>> PRODUCTIONS_AUTORISEES = Map.of(
            "Vache",  List.of("Lait", "Viande"),
            "Chèvre", List.of("Lait", "Viande"),
            "Mouton", List.of("Laine", "Viande"),
            "Poulet", List.of("Œufs", "Viande")
    );

    private static final String STYLE_FIELD_ERROR   = "-fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 4;";
    private static final String STYLE_FIELD_OK      = "-fx-border-color: #27AE60; -fx-border-width: 2; -fx-border-radius: 4;";
    private static final String STYLE_FIELD_DEFAULT = "";
    private static final double QUANTITY_MIN        = 0.01;
    private static final double QUANTITY_MAX        = 99_999.99;

    @FXML
    public void initialize() {
        service         = new ServiceAnimalProduction();
        serviceAnimals  = new Serviceanimals();
        productionsList = FXCollections.observableArrayList();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // ✅ MODIFIÉ : Afficher l'ID de l'animal via getAnimalId()
        colAnimalId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getAnimalId()).asObject()
        );

        colType.setCellValueFactory(new PropertyValueFactory<>("productionType"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("productionDate"));

        setupActionsColumn();

        filterComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "Lait", "Œufs", "Laine", "Viande"
        ));
        filterComboBox.setValue("Tous");

        loadProductions();
        updateStatistics();
    }

    // ══════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════

    @FXML
    private void handleGoToVisiteur() {
        navigateTo("/fxml/VisiteurProductionsView.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            StackPane contentArea = (StackPane) productionsTable.getScene().lookup("#contentArea");
            if (contentArea == null) { System.out.println("❌ contentArea introuvable."); return; }
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) { System.out.println("❌ FXML introuvable : " + fxmlPath); return; }
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
    //  VALIDATION MÉTIER
    // ══════════════════════════════════════════════

    private List<String> getProductionsAutorisees(String typeAnimal) {
        return PRODUCTIONS_AUTORISEES.getOrDefault(typeAnimal, Collections.emptyList());
    }

    private boolean isProductionAutorisee(String typeAnimal, String typeProduction) {
        return getProductionsAutorisees(typeAnimal).contains(typeProduction);
    }

    private boolean validateAnimal(ComboBox<String> combo, Label errorLabel) {
        if (combo.getValue() == null || combo.getValue().isBlank()) {
            applyError(combo, errorLabel, "⚠ Sélectionner un animal.");
            return false;
        }
        applySuccess(combo, errorLabel);
        return true;
    }

    private boolean validateType(ComboBox<String> combo, Label errorLabel) {
        if (combo.getValue() == null || combo.getValue().isBlank()) {
            applyError(combo, errorLabel, "⚠ Choisir un type.");
            return false;
        }
        applySuccess(combo, errorLabel);
        return true;
    }

    private boolean validateProductionMetier(String typeAnimal, String typeProduction, Label errorLabel) {
        if (!isProductionAutorisee(typeAnimal, typeProduction)) {
            errorLabel.setText("⚠ Un(e) " + typeAnimal + " ne peut pas produire : " + typeProduction);
            errorLabel.setTextFill(Color.web("#E74C3C"));
            errorLabel.setVisible(true);
            return false;
        }
        errorLabel.setVisible(false);
        return true;
    }

    private boolean validateQuantity(TextField field, Label errorLabel) {
        String text = field.getText().trim().replace(",", ".");
        if (text.isEmpty()) { applyError(field, errorLabel, "⚠ Quantité obligatoire."); return false; }
        double value;
        try { value = Double.parseDouble(text); }
        catch (NumberFormatException e) { applyError(field, errorLabel, "⚠ Quantité invalide."); return false; }
        if (value < QUANTITY_MIN) { applyError(field, errorLabel, "⚠ Quantité > 0."); return false; }
        if (value > QUANTITY_MAX) { applyError(field, errorLabel, String.format("⚠ Max %.0f.", QUANTITY_MAX)); return false; }
        field.setText(String.valueOf(value));
        applySuccess(field, errorLabel);
        return true;
    }

    private boolean validateDate(DatePicker datePicker, Label errorLabel) {
        LocalDate date = datePicker.getValue();
        if (date == null)                             { applyError(datePicker, errorLabel, "⚠ Date obligatoire.");  return false; }
        if (date.isAfter(LocalDate.now()))            { applyError(datePicker, errorLabel, "⚠ Date future interdite."); return false; }
        if (date.isBefore(LocalDate.of(2000, 1, 1))) { applyError(datePicker, errorLabel, "⚠ Date < 2000 suspecte."); return false; }
        applySuccess(datePicker, errorLabel);
        return true;
    }

    private void applyError(Control field, Label label, String message) {
        field.setStyle(STYLE_FIELD_ERROR);
        label.setText(message);
        label.setTextFill(Color.web("#E74C3C"));
        label.setVisible(true);
    }

    private void applySuccess(Control field, Label label) {
        field.setStyle(STYLE_FIELD_OK);
        label.setVisible(false);
    }

    private void resetField(Control field, Label label) {
        field.setStyle(STYLE_FIELD_DEFAULT);
        label.setVisible(false);
    }

    // ══════════════════════════════════════════════
    //  DIALOG PRODUCTION
    // ══════════════════════════════════════════════

    private Dialog<AnimalProduction> createProductionDialog(String title, AnimalProduction prod) {
        Dialog<AnimalProduction> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType saveButton = new ButtonType("✔ Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        ComboBox<String> animalCombo  = new ComboBox<>();
        ComboBox<String> typeField    = new ComboBox<>();
        TextField        quantityField = new TextField();
        DatePicker       dateField     = new DatePicker();

        quantityField.setPromptText("Ex: 25.5");
        animalCombo.setPromptText("Sélectionner...");
        typeField.setPromptText("Sélectionner...");

        Label errAnimal   = createErrorLabel();
        Label errType     = createErrorLabel();
        Label errMetier   = createErrorLabel();
        Label errQuantity = createErrorLabel();
        Label errDate     = createErrorLabel();

        // ✅ Charger les animaux (objets complets en cache)
        Map<Integer, Animals> animauxMap = new HashMap<>();
        try {
            List<Animals> animals = serviceAnimals.recuperer();
            for (Animals a : animals) {
                String display = a.getId() + " - " + a.getType() + " (" + a.getBreed() + ")";
                animalCombo.getItems().add(display);
                animauxMap.put(a.getId(), a);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // ✅ Pré-remplissage si modification
        if (prod != null) {
            Animals animal = prod.getAnimal();
            if (animal != null) {
                String display = animal.getId() + " - " + animal.getType() + " (" + animal.getBreed() + ")";
                animalCombo.setValue(display);
                typeField.setItems(FXCollections.observableArrayList(getProductionsAutorisees(animal.getType())));
            }
            typeField.setValue(prod.getProductionType());
            quantityField.setText(String.valueOf(prod.getQuantity()));
            dateField.setValue(prod.getProductionDate());
        } else {
            dateField.setValue(LocalDate.now());
        }

        // ✅ Mise à jour dynamique du ComboBox Type selon l'animal sélectionné
        animalCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                validateAnimal(animalCombo, errAnimal);
                try {
                    int animalId = Integer.parseInt(newVal.split(" - ")[0]);
                    Animals animal = animauxMap.get(animalId);
                    if (animal != null) {
                        List<String> autorisees = getProductionsAutorisees(animal.getType());
                        typeField.setItems(FXCollections.observableArrayList(autorisees));
                        typeField.setValue(null);
                        typeField.setPromptText("Choisir parmi : " + String.join(", ", autorisees));
                        errMetier.setVisible(false);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });

        typeField.valueProperty().addListener((obs, o, n) -> { if (n != null) validateType(typeField, errType); });
        quantityField.textProperty().addListener((obs, o, n) -> {
            if (!n.isEmpty()) validateQuantity(quantityField, errQuantity);
            else resetField(quantityField, errQuantity);
        });
        dateField.valueProperty().addListener((obs, o, n) -> { if (n != null) validateDate(dateField, errDate); });

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(4);
        grid.setPadding(new Insets(20, 30, 10, 30));
        grid.setMinWidth(420);

        addRow(grid, 0, "🐄 Animal :",   animalCombo,   errAnimal);
        addRow(grid, 2, "📦 Type :",     typeField,     errType);
        grid.add(errMetier, 1, 4);
        addRow(grid, 5, "⚖ Quantité :", quantityField, errQuantity);
        addRow(grid, 7, "📅 Date :",     dateField,     errDate);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color: #F8F9FA;");

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(saveButton);
        okBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold;");

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                boolean ok = validateAnimal(animalCombo, errAnimal)
                        & validateType(typeField, errType)
                        & validateQuantity(quantityField, errQuantity)
                        & validateDate(dateField, errDate);
                if (!ok) return null;

                try {
                    int     animalId = Integer.parseInt(animalCombo.getValue().split(" - ")[0]);
                    Animals animal   = animauxMap.get(animalId);
                    String  typeProduction = typeField.getValue();

                    // ✅ Validation métier finale
                    if (!validateProductionMetier(animal.getType(), typeProduction, errMetier)) return null;

                    double quantity = Double.parseDouble(quantityField.getText().trim().replace(",", "."));

                    if (prod != null) {
                        prod.setAnimal(animal);  // ✅ Assigner l'objet complet
                        prod.setProductionType(typeProduction);
                        prod.setQuantity(quantity);
                        prod.setProductionDate(dateField.getValue());
                        return prod;
                    } else {
                        return new AnimalProduction(animal, typeProduction, quantity, dateField.getValue());
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "❌ " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
        return dialog;
    }

    private Label createErrorLabel() {
        Label lbl = new Label();
        lbl.setTextFill(Color.web("#E74C3C"));
        lbl.setStyle("-fx-font-size: 11px; -fx-font-style: italic;");
        lbl.setVisible(false);
        lbl.setWrapText(true);
        lbl.setMaxWidth(280);
        return lbl;
    }

    private void addRow(GridPane grid, int row, String labelText, Control field, Label errorLabel) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        grid.add(lbl, 0, row); grid.add(field, 1, row); grid.add(errorLabel, 1, row + 1);
    }

    // ══════════════════════════════════════════════
    //  COLONNE ACTIONS
    // ══════════════════════════════════════════════

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
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
        });
    }

    // ══════════════════════════════════════════════
    //  CRUD
    // ══════════════════════════════════════════════

    private void loadProductions() {
        try {
            List<AnimalProduction> productions = service.recuperer();
            productionsList.clear();
            productionsList.addAll(productions);
            productionsTable.setItems(productionsList);
            lblTotal.setText(String.valueOf(productions.size()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML private void handleAdd() {
        Dialog<AnimalProduction> dialog = createProductionDialog("➕ Nouvelle Production", null);
        Optional<AnimalProduction> result = dialog.showAndWait();
        result.ifPresent(prod -> {
            try { service.ajouter(prod); loadProductions(); updateStatistics();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "✅ Production ajoutée !"); }
            catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage()); }
        });
    }

    private void handleEdit(AnimalProduction prod) {
        Dialog<AnimalProduction> dialog = createProductionDialog("✏️ Modifier", prod);
        Optional<AnimalProduction> result = dialog.showAndWait();
        result.ifPresent(modifiedProd -> {
            try { service.modifier(modifiedProd); loadProductions(); updateStatistics();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "✅ Production modifiée !"); }
            catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage()); }
        });
    }

    private void handleDelete(AnimalProduction prod) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer #" + prod.getId() + " ?");
        confirm.setContentText("Type : " + prod.getProductionType() + "\nQté : " + prod.getQuantity());
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try { service.supprimer(prod); loadProductions(); updateStatistics();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "✅ Supprimée !"); }
                catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage()); }
            }
        });
    }

    @FXML private void handleRefresh() { loadProductions(); updateStatistics(); }

    @FXML private void handleSearch() {
        String search = searchField.getText().toLowerCase();
        productionsTable.setItems(search.isEmpty() ? productionsList :
                productionsList.filtered(p -> p.getProductionType().toLowerCase().contains(search)));
    }

    @FXML private void handleFilter() {
        String filter = filterComboBox.getValue();
        productionsTable.setItems("Tous".equals(filter) ? productionsList :
                productionsList.filtered(p -> p.getProductionType().equals(filter)));
    }

    private void updateStatistics() {
        try {
            lblTotal.setText(String.valueOf(service.compterProductions()));
            lblTotalLait.setText(String.format("%.1f L",   service.calculerProductionTotaleParType("Lait")));
            lblTotalOeufs.setText(String.format("%.0f unités", service.calculerProductionTotaleParType("Œufs")));
            lblMoyenne.setText(String.format("%.2f", service.calculerMoyenneProduction(1)));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content);
        a.showAndWait();
    }
}