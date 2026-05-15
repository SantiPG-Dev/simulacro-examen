package com.examenes.controller;

import com.examenes.MainApp;
import com.examenes.service.ExcelReader;
import com.examenes.util.ThemeToggle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

    private static final int NUM_QUESTIONS = 40;

    @FXML private Label statusLabel;
    @FXML private ThemeToggle themeToggle;

    private MainApp mainApp;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        themeToggle.setDarkMode(mainApp.isDarkTheme());
        themeToggle.setOnToggle(() -> mainApp.toggleTheme());
    }

    // Obtengo la ruta del Excel desde la configuracion
    private String getExcelPath() {
        return mainApp.getConfigService().getExcelPath();
    }

    @FXML
    // Arranco el simulacro si hay preguntas importadas
    private void handleStart(ActionEvent event) {
        try {
            File file = new File(getExcelPath());
            if (!file.exists()) {
                statusLabel.setText("No hay banco de preguntas. Importa datos primero.");
                return;
            }
            mainApp.showSubjectSelect();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    // Abro el submenu de datos (importar, fix webs)
    private void handleData(ActionEvent event) {
        try {
            mainApp.showDataMenu();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    // Reviso y elimino preguntas duplicadas en todas las hojas del Excel
    private void handleReviewDuplicates(ActionEvent event) {
        try {
            File file = new File(getExcelPath());
            if (!file.exists()) {
                statusLabel.setText("No hay archivo Excel que revisar.");
                return;
            }

            Map<String, Integer> results = ExcelReader.deduplicateAllSheets(getExcelPath());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Revisión de Duplicados");
            alert.setHeaderText(null);

            if (results.isEmpty()) {
                alert.setContentText("No se encontraron preguntas duplicadas en ninguna hoja.");
            } else {
                // Muestro cuantos duplicados se eliminaron por cada asignatura
                StringBuilder sb = new StringBuilder("Duplicados eliminados por hoja:\n\n");
                int total = 0;
                for (var entry : results.entrySet()) {
                    sb.append("  • ").append(entry.getKey())
                      .append(": ").append(entry.getValue()).append(" duplicado(s)\n");
                    total += entry.getValue();
                }
                sb.append("\nTotal: ").append(total).append(" duplicado(s) eliminado(s).");
                alert.setContentText(sb.toString());
            }

            alert.showAndWait();
            statusLabel.setText("Revisión completada.");
        } catch (Exception e) {
            statusLabel.setText("Error al revisar duplicados: " + e.getMessage());
        }
    }

    @FXML
    // Abro la pantalla de configuracion
    private void handleConfig(ActionEvent event) {
        try {
            mainApp.showConfig();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    // Abro el archivo LICENSE con el editor de texto por defecto
    private void handleOpenLicense(ActionEvent event) {
        try {
            File licenseFile = new File("LICENSE");
            if (!licenseFile.exists()) {
                licenseFile = new File(
                    System.getProperty("user.dir"), "LICENSE");
            }
            if (licenseFile.exists()) {
                java.awt.Desktop.getDesktop().open(licenseFile);
            } else {
                statusLabel.setText("No se encuentra el archivo LICENSE.");
            }
        } catch (Exception e) {
            statusLabel.setText("Error al abrir licencia: " + e.getMessage());
        }
    }

}
