package controllers;

import services.ServiceRapportPDF;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;

public class RapportController {

    @FXML private Label       lblStatut;
    @FXML private ProgressBar progressBar;
    @FXML private Button      btnGenerer;
    @FXML private Button      btnOuvrir;
    @FXML private Label       lblCheminPDF;
    @FXML private VBox        cardResultat;

    private String dernierPDF = null;

    @FXML
    public void initialize() {
        cardResultat.setVisible(false);
        progressBar.setProgress(0);
    }

    @FXML
    private void handleGenererRapport() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choisir le dossier de destination");
        chooser.setInitialDirectory(
                new File(System.getProperty("user.home") + "/Desktop"));

        Stage stage = (Stage) btnGenerer.getScene().getWindow();
        File  dossier = chooser.showDialog(stage);
        if (dossier == null) return;

        btnGenerer.setDisable(true);
        btnGenerer.setText("Generation en cours...");
        progressBar.setProgress(0);
        lblStatut.setText("Demarrage...");
        cardResultat.setVisible(false);

        new Thread(() -> {
            try {
                ServiceRapportPDF service = new ServiceRapportPDF();
                String pdf = service.genererRapport(
                        dossier.getAbsolutePath(),
                        (msg, pct) -> Platform.runLater(() -> {
                            lblStatut.setText(msg);
                            progressBar.setProgress(pct / 100.0);
                        })
                );
                dernierPDF = pdf;
                Platform.runLater(() -> {
                    lblStatut.setText("Rapport genere avec succes !");
                    progressBar.setProgress(1.0);
                    progressBar.setStyle("-fx-accent: #27AE60;");
                    lblCheminPDF.setText(pdf);
                    cardResultat.setVisible(true);
                    btnGenerer.setDisable(false);
                    btnGenerer.setText("Generer un nouveau rapport");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatut.setText("Erreur : " + e.getMessage());
                    progressBar.setStyle("-fx-accent: #E74C3C;");
                    progressBar.setProgress(1.0);
                    btnGenerer.setDisable(false);
                    btnGenerer.setText("Generer le rapport PDF");
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                });
            }
        }).start();
    }

    @FXML
    private void handleOuvrirPDF() {
        if (dernierPDF == null) return;
        try {
            File f = new File(dernierPDF);
            if (f.exists()) Desktop.getDesktop().open(f);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Impossible d'ouvrir le PDF : " + e.getMessage()).showAndWait();
        }
    }
}