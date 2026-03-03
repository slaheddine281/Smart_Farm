package controllers;

import entities.AnimalProduction;
import entities.Commande;
import entities.CommandeItem;
import services.ServiceAnimalProduction;
import services.ServiceCommande;
import services.ServiceMail;
import services.ServiceStripe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class VisiteurProductionsController {

    private static final Map<String, List<String>> PRODUCTIONS_AUTORISEES = Map.of(
            "Vache",  List.of("Lait", "Viande"),
            "Chèvre", List.of("Lait", "Viande"),
            "Mouton", List.of("Laine", "Viande"),
            "Poulet", List.of("Œufs", "Viande")
    );
    private static final Set<String>    TYPES_AUTORISES = Set.of("Lait", "Œufs", "Laine", "Viande");
    private static final Map<String, Double> PRIX = Map.of(
            "Lait",   2.50, "Œufs",  0.30, "Laine",  8.00, "Viande", 12.00);
    private static final Map<String, String> UNITES = Map.of(
            "Lait",   "L",  "Œufs",  "unité(s)", "Laine", "kg", "Viande", "kg");

    @FXML private TableView<AnimalProduction>              productionsTable;
    @FXML private TableColumn<AnimalProduction, Integer>   colId;
    @FXML private TableColumn<AnimalProduction, Integer>   colAnimalId;
    @FXML private TableColumn<AnimalProduction, String>    colType;
    @FXML private TableColumn<AnimalProduction, Double>    colQuantity;
    @FXML private TableColumn<AnimalProduction, LocalDate> colDate;
    @FXML private TableColumn<AnimalProduction, Void>      colAcheter;
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Label lblTotal;
    @FXML private Label lblTotalLait;
    @FXML private Label lblTotalOeufs;
    @FXML private Label lblTotalLaine;
    @FXML private Label lblTotalViande;
    @FXML private ListView<String> panierListView;
    @FXML private Label lblPanierTotal;
    @FXML private Label lblPanierCount;

    private ServiceAnimalProduction          service;
    private ServiceCommande                  serviceCommande;
    private ObservableList<AnimalProduction> productionsList;
    private Commande                         panier;

    @FXML
    public void initialize() {
        try {
            service         = new ServiceAnimalProduction();
            serviceCommande = new ServiceCommande();
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur connexion", e.getMessage());
            return;
        }
        productionsList = FXCollections.observableArrayList();
        panier          = new Commande();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAnimalId.setCellValueFactory(d ->
                new javafx.beans.property.SimpleIntegerProperty(d.getValue().getAnimalId()).asObject());
        colType.setCellValueFactory(new PropertyValueFactory<>("productionType"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("productionDate"));
        setupAcheterColumn();

        filterComboBox.setItems(FXCollections.observableArrayList("Tous","Lait","Œufs","Laine","Viande"));
        filterComboBox.setValue("Tous");

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
                btnAdd.setStyle("-fx-background-color:linear-gradient(to bottom,#27AE60,#1E8449);" +
                        "-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:7;" +
                        "-fx-cursor:hand;-fx-padding:5 10;");
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
                btnAdd.setDisable(prod.getQuantity() <= 0);
                btnAdd.setText(prod.getQuantity() <= 0 ? "❌ Épuisé" : "🛒 Ajouter");
                setGraphic(box);
            }
        });
    }

    // ══════════════════════════════════════════════
    //  PANIER
    // ══════════════════════════════════════════════

    private void handleAjouterAuPanier(AnimalProduction prod, String qtyText) {
        if (qtyText.isEmpty()) { showAlert(Alert.AlertType.WARNING,"Quantité","⚠ Saisir une quantité."); return; }
        double qty;
        try { qty = Double.parseDouble(qtyText.replace(",",".")); }
        catch (NumberFormatException e) { showAlert(Alert.AlertType.WARNING,"Invalide","⚠ Quantité invalide."); return; }
        if (qty <= 0) { showAlert(Alert.AlertType.WARNING,"Invalide","⚠ Quantité > 0."); return; }
        if (qty > prod.getQuantity()) {
            showAlert(Alert.AlertType.WARNING,"Stock insuffisant",
                    String.format("⚠ Stock : %.2f %s.", prod.getQuantity(),
                            UNITES.getOrDefault(prod.getProductionType(),"")));
            return;
        }
        boolean existe = false;
        for (CommandeItem item : panier.getItems()) {
            if (item.getProductionId() == prod.getId()) {
                double newQty = item.getQuantiteAchat() + qty;
                if (newQty > prod.getQuantity()) {
                    showAlert(Alert.AlertType.WARNING,"Stock","⚠ Total panier > stock."); return;
                }
                item.setQuantiteAchat(newQty); existe = true; break;
            }
        }
        if (!existe) panier.getItems().add(new CommandeItem(
                prod.getId(), prod.getProductionType(), qty,
                PRIX.getOrDefault(prod.getProductionType(), 1.0)));
        panier.recalculerTotal();
        refreshPanierView();
    }

    @FXML private void handleSupprimerDuPanier() {
        int idx = panierListView.getSelectionModel().getSelectedIndex();
        if (idx < 0) { showAlert(Alert.AlertType.INFORMATION,"Sélection","ℹ Sélectionner un article."); return; }
        panier.getItems().remove(idx);
        panier.recalculerTotal();
        refreshPanierView();
    }

    @FXML private void handleViderPanier() {
        panier.getItems().clear(); panier.recalculerTotal(); refreshPanierView();
    }

    private void refreshPanierView() {
        ObservableList<String> lines = FXCollections.observableArrayList();
        for (CommandeItem item : panier.getItems())
            lines.add(String.format("%-10s  %.2f %s  ×  %.2f €  =  %.2f €",
                    item.getProductionType(), item.getQuantiteAchat(),
                    UNITES.getOrDefault(item.getProductionType(),""),
                    item.getPrixUnitaire(), item.getSousTotal()));
        panierListView.setItems(lines);
        lblPanierTotal.setText(String.format("%.2f €", panier.getTotalPrix()));
        lblPanierCount.setText(panier.getItems().size() + " article(s)");
    }

    // ══════════════════════════════════════════════
    //  PAIEMENT STRIPE + EMAIL
    // ══════════════════════════════════════════════

    @FXML
    private void handleValiderCommande() {
        if (panier.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING,"Panier vide","⚠ Ajoutez au moins un produit."); return;
        }
        ouvrirFenetreStripe();
    }

    private void ouvrirFenetreStripe() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("💳  Paiement Stripe — Mode Test");
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color:#F8F9FA;");
        root.setPrefWidth(440);

        // ── Titre ──
        Label titre = new Label("💳  Paiement sécurisé");
        titre.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#2C3E50;");

        Label montantLbl = new Label(String.format("Montant total : %.2f €", panier.getTotalPrix()));
        montantLbl.setStyle("-fx-font-size:15px;-fx-text-fill:#27AE60;-fx-font-weight:bold;");

        Label testNote = new Label("🧪  MODE TEST — Carte Stripe de test :");
        testNote.setStyle("-fx-text-fill:#E67E22;-fx-font-size:12px;-fx-font-weight:bold;");

        Label carteTest = new Label("4242 4242 4242 4242   |   12/34   |   123");
        carteTest.setStyle("-fx-background-color:#FFF3CD;-fx-padding:8 14;" +
                "-fx-background-radius:6;-fx-font-family:'Courier New';" +
                "-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#856404;");

        // ── Formulaire carte ──
        GridPane formCarte = new GridPane();
        formCarte.setHgap(12); formCarte.setVgap(12);
        formCarte.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-padding:20;");

        TextField nomField   = styledField("Nom sur la carte");
        TextField carteField = styledField("Numéro de carte");
        TextField expField   = styledField("MM/AA");
        TextField cvcField   = styledField("CVC");
        expField.setPrefWidth(100); cvcField.setPrefWidth(100);
        carteField.setText("4242 4242 4242 4242");
        expField.setText("12/34"); cvcField.setText("123"); nomField.setText("Test User");

        formCarte.add(new Label("Nom complet"),     0,0); formCarte.add(nomField,   1,0);
        formCarte.add(new Label("Numéro de carte"), 0,1); formCarte.add(carteField, 1,1);
        HBox expCvc = new HBox(10, expField, new Label("CVC"), cvcField);
        expCvc.setAlignment(Pos.CENTER_LEFT);
        formCarte.add(new Label("Expiration"),      0,2); formCarte.add(expCvc,     1,2);

        // ── Champ EMAIL ──
        Separator sep = new Separator();
        Label emailTitre = new Label("📧  Email de confirmation");
        emailTitre.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#2C3E50;");

        GridPane formEmail = new GridPane();
        formEmail.setHgap(12); formEmail.setVgap(10);
        formEmail.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-padding:16;");

        TextField emailField = styledField("ex: client@gmail.com");
        emailField.setPrefWidth(280);
        Label emailNote = new Label("L'email de confirmation sera envoyé à cette adresse.");
        emailNote.setStyle("-fx-text-fill:#95A5A6;-fx-font-size:11px;");

        formEmail.add(new Label("Votre email :"), 0, 0);
        formEmail.add(emailField,                 1, 0);
        formEmail.add(emailNote,                  1, 1);

        // ── Status + Boutons ──
        Label statusLbl = new Label("");
        statusLbl.setStyle("-fx-font-size:12px;");

        Button btnPayer   = new Button("✅  Payer  " + String.format("%.2f €", panier.getTotalPrix()));
        Button btnAnnuler = new Button("Annuler");

        btnPayer.setStyle("-fx-background-color:linear-gradient(to bottom,#27AE60,#1E8449);" +
                "-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:bold;" +
                "-fx-padding:14 20;-fx-background-radius:10;-fx-cursor:hand;");
        btnAnnuler.setStyle("-fx-background-color:transparent;-fx-text-fill:#95A5A6;" +
                "-fx-font-size:12px;-fx-cursor:hand;-fx-border-color:#BDC3C7;" +
                "-fx-border-radius:8;-fx-padding:8 16;");

        btnPayer.setOnAction(e -> {
            String email = emailField.getText().trim();

            // Validation email
            if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
                statusLbl.setTextFill(Color.RED);
                statusLbl.setText("❌ Veuillez entrer un email valide.");
                return;
            }

            btnPayer.setDisable(true);
            btnPayer.setText("⏳  Traitement...");
            statusLbl.setTextFill(Color.ORANGE);
            statusLbl.setText("Connexion à Stripe...");

            new Thread(() -> {
                try {
                    // ── Étape 1 : Paiement Stripe ──
                    ServiceStripe stripe = new ServiceStripe();
                    String stripeId = stripe.payerEnModeTest(
                            panier.getTotalPrix(),
                            "Smart Farm - " + panier.getItems().size() + " article(s)"
                    );

                    javafx.application.Platform.runLater(() -> {
                        statusLbl.setTextFill(Color.ORANGE);
                        statusLbl.setText("✅ Paiement OK. Enregistrement...");
                    });

                    // ── Étape 2 : Enregistrer en BDD ──
                    serviceCommande.enregistrerCommande(panier, stripeId);

                    // ── Étape 3 : Envoyer email ──
                    javafx.application.Platform.runLater(() -> {
                        statusLbl.setText("📧 Envoi de l'email...");
                    });

                    try {
                        ServiceMail mail = new ServiceMail();
                        mail.envoyerConfirmation(email, panier, stripeId);
                    } catch (Exception mailEx) {
                        System.err.println("⚠ Email non envoyé : " + mailEx.getMessage());
                        // On ne bloque pas si l'email échoue
                    }

                    // ── Étape 4 : Succès UI ──
                    Commande commandeFinale = panier;
                    String stripeIdFinal   = stripeId;

                    javafx.application.Platform.runLater(() -> {
                        dialog.close();
                        afficherSucces(stripeIdFinal, email, commandeFinale);
                        panier = new Commande();
                        loadProductions();
                        updateStatistics();
                        refreshPanierView();
                    });

                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        statusLbl.setTextFill(Color.RED);
                        statusLbl.setText("❌ Erreur : " + ex.getMessage());
                        btnPayer.setDisable(false);
                        btnPayer.setText("✅  Réessayer");
                    });
                }
            }).start();
        });

        btnAnnuler.setOnAction(e -> dialog.close());

        HBox btns = new HBox(12, btnAnnuler, btnPayer);
        btns.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                titre, montantLbl, testNote, carteTest,
                new Separator(), formCarte,
                sep, emailTitre, formEmail,
                statusLbl, btns
        );

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private void afficherSucces(String stripeId, String email, Commande commande) {
        String stripeDisplay = stripeId != null && stripeId.length() > 25
                ? stripeId.substring(0, 25) + "..." : stripeId;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("✅  Commande confirmée !");
        alert.setHeaderText("Paiement accepté — Merci !");
        alert.setContentText(String.format(
                "✅ Paiement Stripe confirmé\n" +
                        "🔑 ID Transaction : %s\n\n" +
                        "📧 Email envoyé à : %s\n\n" +
                        "📦 Stock mis à jour automatiquement.\n" +
                        "📊 Statistiques recalculées.",
                stripeDisplay, email
        ));
        alert.showAndWait();
    }

    private TextField styledField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-color:#F8FAFB;-fx-border-color:#DDE1E7;" +
                "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:10 14;-fx-font-size:13px;");
        f.setPrefWidth(220);
        return f;
    }

    // ══════════════════════════════════════════════
    //  CHARGEMENT / FILTRES / STATS
    // ══════════════════════════════════════════════

    private boolean isProductionValide(AnimalProduction prod) {
        if (prod.getAnimal() == null) return false;
        List<String> autorisees = PRODUCTIONS_AUTORISEES.getOrDefault(
                prod.getAnimal().getType(), Collections.emptyList());
        return autorisees.contains(prod.getProductionType());
    }

    private void loadProductions() {
        try {
            productionsList.clear();
            for (AnimalProduction p : service.recuperer())
                if (TYPES_AUTORISES.contains(p.getProductionType()) && isProductionValide(p))
                    productionsList.add(p);
            productionsTable.setItems(productionsList);
        } catch (SQLException e) { showAlert(Alert.AlertType.ERROR,"Erreur",e.getMessage()); }
    }

    @FXML private void handleSearch() { applyFilters(filterComboBox.getValue(), searchField.getText().trim().toLowerCase()); }
    @FXML private void handleFilter() { applyFilters(filterComboBox.getValue(), searchField.getText().trim().toLowerCase()); }

    private void applyFilters(String type, String kw) {
        productionsTable.setItems(productionsList.filtered(p -> {
            boolean t = "Tous".equals(type) || p.getProductionType().equals(type);
            boolean k = kw.isEmpty() || p.getProductionType().toLowerCase().contains(kw);
            return t && k;
        }));
    }

    @FXML private void handleRefresh() {
        searchField.clear(); filterComboBox.setValue("Tous"); loadProductions(); updateStatistics();
    }

    private void updateStatistics() {
        try {
            lblTotal.setText(String.valueOf(productionsList.size()));
            lblTotalLait.setText(String.format("%.1f L",       service.calculerProductionTotaleParType("Lait")));
            lblTotalOeufs.setText(String.format("%.0f unités", service.calculerProductionTotaleParType("Œufs")));
            lblTotalLaine.setText(String.format("%.1f kg",     service.calculerProductionTotaleParType("Laine")));
            lblTotalViande.setText(String.format("%.1f kg",    service.calculerProductionTotaleParType("Viande")));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}