package com.examenes.controller;

import com.examenes.MainApp;
import com.examenes.model.Question;
import com.examenes.service.ExcelReader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


public class SubjectSelectController implements Initializable {

    private static final int NUM_QUESTIONS = 40;
    private static final int TIME_MINUTES = 40;

    @FXML private VBox buttonContainer;
    @FXML private Label statusLabel;
    private MainApp mainApp;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    private String getExcelPath() {
        return mainApp.getConfigService().getExcelPath();
    }

    // Leo las asignaturas del Excel y creo un boton por cada una
    public void loadSubjects() {
        try {
            buttonContainer.getChildren().clear();
            Map<String, Integer> subjectCounts = ExcelReader.getSubjectQuestionCounts(getExcelPath());
            if (subjectCounts.isEmpty()) {
                statusLabel.setText("El banco de preguntas esta vacio.");
                return;
            }
            for (Map.Entry<String, Integer> entry : subjectCounts.entrySet()) {
                String subject = entry.getKey();
                int count = entry.getValue();
                String label = count + " preguntas";
                Button btn = new Button(subject + "  (" + label + ")");
                btn.setMaxWidth(360);
                btn.setPrefHeight(48);
                btn.getStyleClass().add("primary-button");
                // Al pulsar el boton, arranco el simulacro de esa asignatura
                btn.setOnAction(e -> startExam(subject));
                buttonContainer.getChildren().add(btn);
            }
        } catch (Exception e) {
            statusLabel.setText("Error (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }
    }

    // Cargo las preguntas de la asignatura, las mezclo y lanzo el simulacro
    private void startExam(String subject) {
        try {
            List<Question> questions = ExcelReader.readQuestions(getExcelPath(), subject);
            if (questions.isEmpty()) {
                statusLabel.setText("No hay preguntas para " + subject);
                return;
            }
            Collections.shuffle(questions);
            // Cojo las primeras 40 preguntas como maximo
            int numQuestions = Math.min(NUM_QUESTIONS, questions.size());
            List<Question> examQuestions = questions.subList(0, numQuestions);
            mainApp.showExam(examQuestions, subject, TIME_MINUTES);
        } catch (Exception e) {
            statusLabel.setText("Error (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            mainApp.showMenu();
        } catch (Exception e) {
            statusLabel.setText("Error (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }
    }

}
