package com.examenes.controller;

import com.examenes.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class DataMenuController implements Initializable {

    @FXML private Label statusLabel;
    private MainApp mainApp;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleImport(ActionEvent event) {
        try {
            mainApp.showImportMenu();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleOrganizeDownloads(ActionEvent event) {
        String downloadsPath = mainApp.getConfigService().getDownloadsFolder();
        String htmlFolder = mainApp.getConfigService().getHtmlFolder();

        if (downloadsPath == null || downloadsPath.isBlank()) {
            statusLabel.setText("Configura la carpeta de descargas en Ajustes primero.");
            return;
        }

        try {
            java.io.File downloadsDir = new java.io.File(downloadsPath);
            if (!downloadsDir.exists() || !downloadsDir.isDirectory()) {
                statusLabel.setText("La carpeta de descargas no existe: " + downloadsPath);
                return;
            }

            java.io.File htmlDir = new java.io.File(htmlFolder);
            if (!htmlDir.exists()) {
                htmlDir.mkdirs();
            }

            java.io.File[] files = downloadsDir.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".html") || name.toLowerCase().endsWith(".htm"));
            if (files == null || files.length == 0) {
                statusLabel.setText("No se encontraron archivos HTML en la carpeta de descargas.");
                return;
            }

            int moved = 0;
            for (java.io.File f : files) {
                java.io.File dest = new java.io.File(htmlDir, f.getName());
                if (dest.exists()) continue;
                java.nio.file.Files.move(f.toPath(), dest.toPath());
                moved++;
            }

            statusLabel.setText("Organizados " + moved + " archivos HTML de descargas a " + htmlFolder);
        } catch (Exception e) {
            statusLabel.setText("Error al organizar: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            mainApp.showMenu();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
}
