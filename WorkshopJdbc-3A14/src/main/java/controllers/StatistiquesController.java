package controllers;

import services.ServiceStatistiques;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class StatistiquesController {

    // ── KPI Labels ───────────────────────────────────────────
    @FXML private Label lblTotalProductions;
    @FXML private Label lblTotalAnimaux;
    @FXML private Label lblTotalLait;
    @FXML private Label lblTotalOeufs;
    @FXML private Label lblTotalLaine;
    @FXML private Label lblTotalViande;
    @FXML private Label lblMoyenneJour;
    @FXML private Label lblTotalCommandes;

    // ── Santé ────────────────────────────────────────────────
    @FXML private Label lblSanteBon;
    @FXML private Label lblSanteSurv;
    @FXML private Label lblSanteMalade;
    @FXML private Label lblTauxSante;

    // ── Top 5 animaux ────────────────────────────────────────
    @FXML private TableView<Object[]>  topAnimauxTable;
    @FXML private TableColumn<Object[], String> colTopRank;
    @FXML private TableColumn<Object[], String> colTopAnimal;
    @FXML private TableColumn<Object[], String> colTopType;
    @FXML private TableColumn<Object[], String> colTopQty;

    // ── Prod par type ─────────────────────────────────────────
    @FXML private TableView<Object[]>  prodParTypeTable;
    @FXML private TableColumn<Object[], String> colPTAnimal;
    @FXML private TableColumn<Object[], String> colPTProd;
    @FXML private TableColumn<Object[], String> colPTQty;
    @FXML private TableColumn<Object[], String> colPTCount;

    // ── Dernières productions ────────────────────────────────
    @FXML private TableView<Object[]>  dernieresProdsTable;
    @FXML private TableColumn<Object[], String> colDPAnimal;
    @FXML private TableColumn<Object[], String> colDPType;
    @FXML private TableColumn<Object[], String> colDPQty;
    @FXML private TableColumn<Object[], String> colDPDate;

    // ── CA ───────────────────────────────────────────────────
    @FXML private TableView<Object[]>  caTable;
    @FXML private TableColumn<Object[], String> colCAProd;
    @FXML private TableColumn<Object[], String> colCAQty;
    @FXML private TableColumn<Object[], String> colCAPrix;
    @FXML private TableColumn<Object[], String> colCATotal;
    @FXML private Label lblCaTotal;
    @FXML private Label lblNbCommandes;
    @FXML private Label lblPanierMoyen;

    private ServiceStatistiques service;

    @FXML
    public void initialize() {
        try {
            service = new ServiceStatistiques();
        } catch (RuntimeException e) {
            showAlert("Erreur connexion", e.getMessage());
            return;
        }

        setupColumns();
        loadAll();
    }

    private void setupColumns() {
        // Top animaux
        colTopRank.setCellValueFactory(d   -> new SimpleStringProperty(String.valueOf(d.getValue()[0])));
        colTopAnimal.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue()[1])));
        colTopType.setCellValueFactory(d   -> new SimpleStringProperty(String.valueOf(d.getValue()[2])));
        colTopQty.setCellValueFactory(d    -> new SimpleStringProperty(String.valueOf(d.getValue()[3])));

        // Prod par type
        colPTAnimal.setCellValueFactory(d  -> new SimpleStringProperty(String.valueOf(d.getValue()[0])));
        colPTProd.setCellValueFactory(d    -> new SimpleStringProperty(String.valueOf(d.getValue()[1])));
        colPTQty.setCellValueFactory(d     -> new SimpleStringProperty(String.valueOf(d.getValue()[2])));
        colPTCount.setCellValueFactory(d   -> new SimpleStringProperty(String.valueOf(d.getValue()[3])));

        // Dernières productions
        colDPAnimal.setCellValueFactory(d  -> new SimpleStringProperty(String.valueOf(d.getValue()[0])));
        colDPType.setCellValueFactory(d    -> new SimpleStringProperty(String.valueOf(d.getValue()[1])));
        colDPQty.setCellValueFactory(d     -> new SimpleStringProperty(String.valueOf(d.getValue()[2])));
        colDPDate.setCellValueFactory(d    -> new SimpleStringProperty(String.valueOf(d.getValue()[3])));

        // CA
        colCAProd.setCellValueFactory(d    -> new SimpleStringProperty(String.valueOf(d.getValue()[0])));
        colCAQty.setCellValueFactory(d     -> new SimpleStringProperty(String.valueOf(d.getValue()[1])));
        colCAPrix.setCellValueFactory(d    -> new SimpleStringProperty(String.valueOf(d.getValue()[2])));
        colCATotal.setCellValueFactory(d   -> new SimpleStringProperty(String.valueOf(d.getValue()[3])));

        // Couleur alternée sur les tableaux
        colorizeTable(topAnimauxTable);
        colorizeTable(prodParTypeTable);
        colorizeTable(dernieresProdsTable);
        colorizeTable(caTable);
    }

    private <T> void colorizeTable(TableView<T> table) {
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); return; }
                setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color: #FAFCFD;"
                        : "-fx-background-color: white;");
            }
        });
    }

    private void loadAll() {
        try {
            // ── KPIs ──
            lblTotalProductions.setText(String.valueOf(service.getTotalProductions()));
            lblTotalAnimaux.setText(String.valueOf(service.getTotalAnimaux()));
            lblTotalLait.setText(String.format("%.1f", service.getTotalParType("Lait")));
            lblTotalOeufs.setText(String.format("%.0f", service.getTotalParType("Œufs")));
            lblTotalLaine.setText(String.format("%.1f", service.getTotalParType("Laine")));
            lblTotalViande.setText(String.format("%.1f", service.getTotalParType("Viande")));
            lblMoyenneJour.setText(String.format("%.1f", service.getMoyenneParJour()));
            lblTotalCommandes.setText(String.valueOf(service.getTotalCommandes()));

            // ── Santé ──
            Map<String, Integer> sante = service.getSanteStats();
            int bon  = sante.get("bon");
            int surv = sante.get("surveillance");
            int mal  = sante.get("malade");
            int total = bon + surv + mal;
            lblSanteBon.setText(String.valueOf(bon));
            lblSanteSurv.setText(String.valueOf(surv));
            lblSanteMalade.setText(String.valueOf(mal));
            lblTauxSante.setText(total > 0
                    ? String.format("%.0f %%", (bon * 100.0) / total)
                    : "— %");

            // ── Top 5 ──
            List<Object[]> top5 = service.getTop5Animaux();
            ObservableList<Object[]> topList = FXCollections.observableArrayList(top5);
            topAnimauxTable.setItems(topList);

            // ── Prod par type ──
            List<Object[]> parType = service.getProdParTypeAnimal();
            prodParTypeTable.setItems(FXCollections.observableArrayList(parType));

            // ── Dernières ──
            List<Object[]> dernieres = service.getDernieresProductions();
            dernieresProdsTable.setItems(FXCollections.observableArrayList(dernieres));

            // ── CA ──
            List<Object[]> ca = service.getCAParProduit();
            caTable.setItems(FXCollections.observableArrayList(ca));
            lblCaTotal.setText(String.format("%.2f €", service.getCaTotal()));
            lblNbCommandes.setText(String.valueOf(service.getNbCommandes()));
            double caVal = service.getCaTotal();
            int nbCmd    = service.getNbCommandes();
            lblPanierMoyen.setText(nbCmd > 0
                    ? String.format("%.2f €", caVal / nbCmd)
                    : "0.00 €");

        } catch (SQLException e) {
            showAlert("Erreur chargement stats", e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadAll();
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}