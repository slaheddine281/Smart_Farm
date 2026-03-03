package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.net.URL;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        loadView("/fxml/AnimalsView.fxml");
        statusLabel.setText("Bienvenue dans Smart Farm ! 🌱");
    }

    @FXML
    public void showAnimalsView() {
        System.out.println("→ Chargement AnimalsView");
        loadView("/fxml/AnimalsView.fxml");
        statusLabel.setText("Vue Animaux");
    }

    @FXML
    public void showProductionsView() {
        System.out.println("→ Chargement ProductionsView");
        loadView("/fxml/AnimalProductionsView.fxml");
        statusLabel.setText("Vue Productions");
    }


    @FXML
    public void showRapportView() {
        System.out.println("→ Chargement RapportView");
        loadView("/fxml/RapportView.fxml");
        if (statusLabel != null) statusLabel.setText("Rapport IA");
    }

    @FXML
    public void showStatistiquesView() {
        System.out.println("→ Chargement StatistiquesView");
        loadView("/fxml/StatistiquesView.fxml");
        statusLabel.setText("Vue Statistiques");
    }

    private void loadView(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);

            if (resource == null) {
                System.out.println("❌ FICHIER INTROUVABLE: " + fxmlPath);
                statusLabel.setText("❌ Fichier introuvable: " + fxmlPath);
                return;
            }

            System.out.println("✅ Fichier trouvé: " + resource);
            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            System.out.println("✅ Vue chargée!");

        } catch (Exception e) {
            System.out.println("❌ ERREUR: " + e.getMessage());
            System.out.println("❌ CAUSE: " + e.getCause());
            e.printStackTrace();
            if (statusLabel != null)
                statusLabel.setText("Erreur chargement vue");
        }
    }
}