package com.examenes.controller;

import com.examenes.MainApp;
import com.examenes.model.Question;
import com.examenes.service.ExcelReader;
import com.examenes.service.HtmlParser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MenuController implements Initializable {

    private static final String EXCEL_PATH = "excel/BateriaPreguntas.xlsx";
    private static final int NUM_QUESTIONS = 40;
    private static final int TIME_MINUTES = 60;

    @FXML private ComboBox<String> subjectCombo;
    @FXML private Label statusLabel;
    @FXML private VBox loadingOverlay;

    private MainApp mainApp;
    private List<Question> allQuestions;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadSubjects();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    private void loadSubjects() {
        subjectCombo.getItems().add("Todas las asignaturas");
        subjectCombo.getSelectionModel().select(0);

        try {
            File file = new File(EXCEL_PATH);
            if (!file.exists()) {
                statusLabel.setText("No se encuentra el archivo de preguntas. Usa 'Generar Preguntas de Ejemplo'.");
                return;
            }
            List<String> subjects = ExcelReader.getSubjects(EXCEL_PATH);
            subjectCombo.getItems().addAll(subjects);
            statusLabel.setText("Archivo cargado: " + subjects.size() + " asignaturas disponibles");
        } catch (Exception e) {
            statusLabel.setText("Error al leer el archivo: " + e.getMessage());
        }
    }

    @FXML
    private void handleStart(ActionEvent event) {
        String selectedSubject = subjectCombo.getSelectionModel().getSelectedItem();

        try {
            File file = new File(EXCEL_PATH);
            if (!file.exists()) {
                statusLabel.setText("Primero genera las preguntas de ejemplo.");
                return;
            }

            allQuestions = ExcelReader.readQuestions(EXCEL_PATH);

            if (!"Todas las asignaturas".equals(selectedSubject)) {
                allQuestions = allQuestions.stream()
                        .filter(q -> q.getSubject().equals(selectedSubject))
                        .toList();
            }

            if (allQuestions.isEmpty()) {
                statusLabel.setText("No hay preguntas para la asignatura seleccionada.");
                return;
            }

            Collections.shuffle(allQuestions);

            int numQuestions = Math.min(NUM_QUESTIONS, allQuestions.size());
            List<Question> examQuestions = allQuestions.subList(0, numQuestions);

            int timeMinutes = Math.max(10, numQuestions * 90 / 60);

            mainApp.showExam(examQuestions, selectedSubject, timeMinutes);

        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleHistory(ActionEvent event) {
        try {
            mainApp.showHistory();
        } catch (Exception e) {
            showAlert("Error", "No se pudo abrir el historial: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateSample(ActionEvent event) {
        try {
            ExcelReader.createSampleExcel(EXCEL_PATH);
            statusLabel.setText("Archivo BateriaPreguntas.xlsx creado con 50 preguntas de ejemplo.");
            loadSubjects();
        } catch (IOException e) {
            statusLabel.setText("Error al generar: " + e.getMessage());
        }
    }

    @FXML
    private void handleImportHtml(ActionEvent event) {
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
            loadSubjects();
        } catch (Exception e) {
            statusLabel.setText("Error al importar: " + e.getMessage());
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Platform.exit();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
