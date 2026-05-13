package com.examenes.controller;

import com.examenes.MainApp;
import com.examenes.service.ConfigService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ConfigController implements Initializable {

    @FXML private TextField excelPathField;
    @FXML private TextField htmlFolderField;
    @FXML private TextField githubRepoField;
    @FXML private PasswordField githubTokenField;
    @FXML private Label statusLabel;

    private MainApp mainApp;
    private ConfigService config;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = new ConfigService();
        loadSettings();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    private void loadSettings() {
        excelPathField.setText(config.getExcelPath());
        htmlFolderField.setText(config.getHtmlFolder());
        githubRepoField.setText(config.getGithubRepo());
        githubTokenField.setText(config.getGithubToken());
    }

    @FXML
    private void handleBrowseExcel(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar archivo Excel");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files", "*.xlsx"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            excelPathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleBrowseHtml(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Seleccionar carpeta HTML");
        File dir = dc.showDialog(null);
        if (dir != null) {
            htmlFolderField.setText(dir.getAbsolutePath());
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        config.setExcelPath(excelPathField.getText());
        config.setHtmlFolder(htmlFolderField.getText());
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
