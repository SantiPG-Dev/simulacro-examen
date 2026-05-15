package com.examenes.controller;

import com.examenes.MainApp;
import com.examenes.service.ConfigService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ConfigController implements Initializable {

    @FXML private TextField htmlFolderField;
    @FXML private TextField downloadsFolderField;
    @FXML private TextField githubRepoField;
    @FXML private PasswordField githubTokenField;
    @FXML private Label statusLabel;
    private MainApp mainApp;
    private ConfigService config;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        this.config = mainApp.getConfigService();
        loadSettings();
    }

    // Cargo los valores guardados en los campos del formulario
    private void loadSettings() {
        htmlFolderField.setText(config.getHtmlFolder());
        downloadsFolderField.setText(config.getDownloadsFolder());
        githubRepoField.setText(config.getGithubRepo());
        githubTokenField.setText(config.getGithubToken());
    }

    @FXML
    // Abro un selector de carpeta para la carpeta HTML
    private void handleBrowseHtml(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Seleccionar carpeta HTML");
        File dir = dc.showDialog(null);
        if (dir != null) {
            htmlFolderField.setText(dir.getAbsolutePath());
        }
    }

    @FXML
    // Abro un selector de carpeta para la carpeta de descargas
    private void handleBrowseDownloads(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Seleccionar carpeta de descargas");
        File dir = dc.showDialog(null);
        if (dir != null) {
            downloadsFolderField.setText(dir.getAbsolutePath());
        }
    }

    @FXML
    // Guardo todos los valores de configuracion en el archivo de propiedades
    private void handleSave(ActionEvent event) {
        config.setHtmlFolder(htmlFolderField.getText());
        config.setDownloadsFolder(downloadsFolderField.getText());
        config.setGithubRepo(githubRepoField.getText());
        config.setGithubToken(githubTokenField.getText());
        config.save();
        statusLabel.setText("Configuracion guardada.");
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
