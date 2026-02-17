package controllers;

import entities.AnimalProduction;
import entities.Commande;
import entities.CommandeItem;
import services.ServiceAnimalProduction;
import services.ServiceCommande;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public class VisiteurProductionsController {

    // ── Prix fixes par type ──────────────────────
    private static final Set<String> TYPES_AUTORISES = Set.of("Lait", "Œufs", "Laine", "Viande");
    private static final Map<String, Double> PRIX = Map.of(
            "Lait",   2.50,
            "Œufs",  0.30,
            "Laine",  8.00,
            "Viande", 12.00
    );
    private static final Map<String, String> UNITES = Map.of(
            "Lait",   "L",
            "Œufs",  "unité(s)",
            "Laine",  "kg",
            "Viande", "kg"
    );

    // ── FXML — Tableau ───────────────────────────
    @FXML private TableView<AnimalProduction>          productionsTable;
    @FXML private TableColumn<AnimalProduction, Integer>   colId;
    @FXML private TableColumn<AnimalProduction, Integer>   colAnimalId;
    @FXML private TableColumn<AnimalProduction, String>    colType;
    @FXML private TableColumn<AnimalProduction, Double>    colQuantity;
    @FXML private TableColumn<AnimalProduction, LocalDate> colDate;
    @FXML private TableColumn<AnimalProduction, Void>      colAcheter;

    // ── FXML — Recherche ─────────────────────────
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterComboBox;

    // ── FXML — Stats ─────────────────────────────
    @FXML private Label lblTotal;
    @FXML private Label lblTotalLait;
    @FXML private Label lblTotalOeufs;
    @FXML private Label lblTotalLaine;
    @FXML private Label lblTotalViande;

    // ── FXML — Panier ────────────────────────────
    @FXML private ListView<String> panierListView;
    @FXML private Label            lblPanierTotal;
    @FXML private Label            lblPanierCount;

    // ── Données ──────────────────────────────────
    private ServiceAnimalProduction          service;
    private ServiceCommande                  serviceCommande;
    private ObservableList<AnimalProduction> productionsList;
    private Commande                         panier;

    // ══════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════
    @FXML
    public void initialize() {
        service         = new ServiceAnimalProduction();
        serviceCommande = new ServiceCommande();
        productionsList = FXCollections.observableArrayList();
        panier          = new Commande();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAnimalId.setCellValueFactory(new PropertyValueFactory<>("animalId"));
        colType.setCellValueFactory(new PropertyValueFactory<>("productionType"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("productionDate"));
        setupAcheterColumn();

        filterComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "Lait", "Œufs", "Laine", "Viande"));
        filterComboBox.setValue("Tous");

        productionsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(AnimalProduction item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); return; }
                switch (item.getProductionType()) {
                    case "Lait"   -> setStyle("-fx-background-color: #EBF5FB;");
                    case "Œufs"  -> setStyle("-fx-background-color: #FEF9E7;");
                    case "Laine" -> setStyle("-fx-background-color: #F9EBEA;");
                    case "Viande"-> setStyle("-fx-background-color: #FDEDEC;");
                    default       -> setStyle("");
                }
            }
        });

        loadProductions();
        updateStatistics();
        refreshPanierView();
    }

    // ══════════════════════════════════════════════
    //  COLONNE ACHETER
    // ══════════════════════════════════════════════

    private void setupAcheterColumn() {
        colAcheter.setCellFactory(param -> new TableCell<>() {
            private final TextField qtyField = new TextField("1");
            private final Button    btnAdd   = new Button("🛒 Ajouter");
            private final HBox      box      = new HBox(6, qtyField, btnAdd);
            {
                qtyField.setPrefWidth(55);
                qtyField.setStyle("-fx-background-radius:6;-fx-border-radius:6;" +
                        "-fx-border-color:#BDC3C7;-fx-padding:4 6 4 6;");
                btnAdd.setStyle("-fx-background-color:linear-gradient(to bottom,#27AE60,#1E8449);" +
                        "-fx-text-fill:white;-fx-font-weight:bold;" +
                        "-fx-background-radius:7;-fx-cursor:hand;-fx-padding:5 10 5 10;");
                // Filtre chiffres uniquement
                qtyField.textProperty().addListener((obs, o, n) -> {
                    if (!n.matches("\\d*\\.?\\d*")) qtyField.setText(o);
                });
                btnAdd.setOnAction(e -> {
                    AnimalProduction prod = getTableView().getItems().get(getIndex());
                    handleAjouterAuPanier(prod, qtyField.getText().trim());
                    qtyField.setText("1");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                AnimalProduction prod = getTableView().getItems().get(getIndex());
                boolean ok = prod.getQuantity() > 0;
                btnAdd.setDisable(!ok);
                btnAdd.setText(ok ? "🛒 Ajouter" : "❌ Épuisé");
                setGraphic(box);
            }
        });
    }

    // ══════════════════════════════════════════════
    //  LOGIQUE PANIER
    // ══════════════════════════════════════════════

    private void handleAjouterAuPanier(AnimalProduction prod, String qtyText) {
        if (qtyText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Quantité manquante",
                    "⚠ Veuillez saisir une quantité.");
            return;
        }
        double qty;
        try {
            qty = Double.parseDouble(qtyText.replace(",", "."));
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalide", "⚠ Quantité invalide."); return;
        }
        if (qty <= 0) {
            showAlert(Alert.AlertType.WARNING, "Invalide", "⚠ Quantité doit être > 0."); return;
        }
        if (qty > prod.getQuantity()) {
            showAlert(Alert.AlertType.WARNING, "Stock insuffisant",
                    String.format("⚠ Stock disponible : %.2f %s.",
                            prod.getQuantity(),
                            UNITES.getOrDefault(prod.getProductionType(), "")));
            return;
        }

        // Si déjà dans le panier, incrémenter
        boolean existe = false;
        for (CommandeItem item : panier.getItems()) {
            if (item.getProductionId() == prod.getId()) {
                double newQty = item.getQuantiteAchat() + qty;
                if (newQty > prod.getQuantity()) {
                    showAlert(Alert.AlertType.WARNING, "Stock insuffisant",
                            String.format("⚠ Total panier (%.2f) > stock (%.2f).",
                                    newQty, prod.getQuantity()));
                    return;
                }
                item.setQuantiteAchat(newQty);
                existe = true;
                break;
            }
        }
        if (!existe) {
            panier.getItems().add(new CommandeItem(
                    prod.getId(), prod.getProductionType(), qty,
                    PRIX.getOrDefault(prod.getProductionType(), 1.0)
            ));
        }
        panier.recalculerTotal();
        refreshPanierView();
    }

    @FXML
    private void handleSupprimerDuPanier() {
        int idx = panierListView.getSelectionModel().getSelectedIndex();
        if (idx < 0) {
            showAlert(Alert.AlertType.INFORMATION, "Sélection requise",
                    "ℹ Sélectionnez un article à supprimer."); return;
        }
        panier.getItems().remove(idx);
        panier.recalculerTotal();
        refreshPanierView();
    }

    @FXML
    private void handleViderPanier() {
        panier.getItems().clear();
        panier.recalculerTotal();
        refreshPanierView();
    }

    @FXML
    private void handleValiderCommande() {
        if (panier.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Panier vide",
                    "⚠ Ajoutez au moins un produit."); return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la commande");
        confirm.setHeaderText("Valider votre commande ?");
        confirm.setContentText(String.format(
                "Articles : %d\nTotal : %.2f €\n\nLes stocks seront mis à jour.",
                panier.getItems().size(), panier.getTotalPrix()));

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    serviceCommande.enregistrerCommande(panier);
                    showAlert(Alert.AlertType.INFORMATION, "✅ Commande confirmée !",
                            String.format("Commande enregistrée !\nTotal : %.2f €",
                                    panier.getTotalPrix()));
                    panier = new Commande();
                    loadProductions();
                    updateStatistics();
                    refreshPanierView();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "❌ " + e.getMessage());
                }
            }
        });
    }

    private void refreshPanierView() {
        ObservableList<String> lines = FXCollections.observableArrayList();
        for (CommandeItem item : panier.getItems()) {
            lines.add(String.format("%-10s  %.2f %s  ×  %.2f €  =  %.2f €",
                    item.getProductionType(),
                    item.getQuantiteAchat(),
                    UNITES.getOrDefault(item.getProductionType(), ""),
                    item.getPrixUnitaire(),
                    item.getSousTotal()));
        }
        panierListView.setItems(lines);
        lblPanierTotal.setText(String.format("%.2f €", panier.getTotalPrix()));
        lblPanierCount.setText(panier.getItems().size() + " article(s)");
    }

    // ══════════════════════════════════════════════
    //  CHARGEMENT / FILTRES / STATS
    // ══════════════════════════════════════════════

    private void loadProductions() {
        try {
            productionsList.clear();
            service.recuperer().stream()
                    .filter(p -> TYPES_AUTORISES.contains(p.getProductionType()))
                    .forEach(productionsList::add);
            productionsTable.setItems(productionsList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML private void handleSearch() {
        applyFilters(filterComboBox.getValue(), searchField.getText().trim().toLowerCase());
    }
    @FXML private void handleFilter() {
        applyFilters(filterComboBox.getValue(), searchField.getText().trim().toLowerCase());
    }
    private void applyFilters(String type, String kw) {
        productionsTable.setItems(productionsList.filtered(p -> {
            boolean t = "Tous".equals(type) || p.getProductionType().equals(type);
            boolean k = kw.isEmpty() || p.getProductionType().toLowerCase().contains(kw)
                    || String.valueOf(p.getQuantity()).contains(kw)
                    || p.getProductionDate().toString().contains(kw);
            return t && k;
        }));
    }

    @FXML private void handleRefresh() {
        searchField.clear();
        filterComboBox.setValue("Tous");
        loadProductions();
        updateStatistics();
    }

    private void updateStatistics() {
        try {
            lblTotal.setText(String.valueOf(productionsList.size()));
            lblTotalLait.setText(String.format("%.1f L",   service.calculerProductionTotaleParType("Lait")));
            lblTotalOeufs.setText(String.format("%.0f unités", service.calculerProductionTotaleParType("Œufs")));
            lblTotalLaine.setText(String.format("%.1f kg",  service.calculerProductionTotaleParType("Laine")));
            lblTotalViande.setText(String.format("%.1f kg", service.calculerProductionTotaleParType("Viande")));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content);
        a.showAndWait();
    }
}