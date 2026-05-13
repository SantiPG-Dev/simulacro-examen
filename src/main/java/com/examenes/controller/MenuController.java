package com.examenes.controller;

import com.examenes.MainApp;
import com.examenes.model.Question;
import com.examenes.service.ExcelReader;
import com.examenes.service.HtmlParser;
import com.examenes.util.ThemeToggle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MenuController implements Initializable {

    private static final String EXCEL_PATH = "excel/BateriaPreguntas.xlsx";
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

    @FXML
    private void handleStart(ActionEvent event) {
        try {
            File file = new File(EXCEL_PATH);
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
    private void handleImport(ActionEvent event) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Desde HTML", "Desde HTML", "Desde Excel");
        dialog.setTitle("Importar Datos");
        dialog.setHeaderText("Selecciona el origen de importacion");
        dialog.setContentText("Origen:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        if ("Desde HTML".equals(result.get())) {
            importHtml();
        } else {
            importExcel();
        }
    }

    private void importHtml() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Importar preguntas desde HTML");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML files", "*.html", "*.htm"));
        File selected = fc.showOpenDialog(null);
        if (selected == null) return;

        try {
            List<Question> all = HtmlParser.parse(selected);
            if (all.isEmpty()) {
                statusLabel.setText("No se encontraron preguntas en el archivo.");
                return;
            }

            Map<String, List<Question>> bySubject = all.stream()
                    .collect(Collectors.groupingBy(Question::getSubject));

            int totalAdded = 0;
            for (var entry : bySubject.entrySet()) {
                ExcelReader.appendQuestions(EXCEL_PATH, entry.getKey(), entry.getValue());
                totalAdded += entry.getValue().size();
            }

            statusLabel.setText("Importadas " + totalAdded + " preguntas de " + bySubject.size() + " asignaturas.");
        } catch (Exception e) {
            statusLabel.setText("Error al importar: " + e.getMessage());
        }
    }

    private void importExcel() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Importar preguntas desde Excel");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files", "*.xlsx"));
        File selected = fc.showOpenDialog(null);
        if (selected == null) return;

        try {
            List<Question> imported = ExcelReader.readQuestions(selected.getAbsolutePath());
            if (imported.isEmpty()) {
                statusLabel.setText("No se encontraron preguntas en el archivo.");
                return;
            }

            Map<String, List<Question>> bySubject = imported.stream()
                    .collect(Collectors.groupingBy(Question::getSubject));

            int totalAdded = 0;
            for (var entry : bySubject.entrySet()) {
                ExcelReader.appendQuestions(EXCEL_PATH, entry.getKey(), entry.getValue());
                totalAdded += entry.getValue().size();
            }

            statusLabel.setText("Importadas " + totalAdded + " preguntas desde Excel.");
        } catch (Exception e) {
            statusLabel.setText("Error al importar: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport(ActionEvent event) {
        try {
            File file = new File(EXCEL_PATH);
            if (!file.exists()) {
                statusLabel.setText("No hay datos para exportar.");
                return;
            }

            FileChooser fc = new FileChooser();
            fc.setTitle("Exportar banco de preguntas");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files", "*.xlsx"));
            fc.setInitialFileName("ExportacionPreguntas.xlsx");
            File dest = fc.showSaveDialog(null);
            if (dest == null) return;

            java.nio.file.Files.copy(file.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            statusLabel.setText("Exportado a: " + dest.getName());
        } catch (Exception e) {
            statusLabel.setText("Error al exportar: " + e.getMessage());
        }
    }

    @FXML
    private void handleConfig(ActionEvent event) {
        try {
            mainApp.showConfig();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

}
