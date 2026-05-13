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

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static com.examenes.service.ExcelReader.createSampleExcel;

public class SubjectSelectController implements Initializable {

    private static final String EXCEL_PATH = "excel/BateriaPreguntas.xlsx";
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

    public void loadSubjects() {
        try {
            File file = new File(EXCEL_PATH);
            if (!file.exists()) {
                createSampleExcel(EXCEL_PATH);
            }
            List<String> subjects = ExcelReader.getSubjects(EXCEL_PATH);
            if (subjects.isEmpty()) {
                statusLabel.setText("El banco de preguntas esta vacio.");
                return;
            }
            for (String subject : subjects) {
                Button btn = new Button(subject);
                btn.setMaxWidth(360);
                btn.setPrefHeight(48);
                btn.getStyleClass().add("primary-button");
                btn.setOnAction(e -> startExam(subject));
                buttonContainer.getChildren().add(btn);
            }
        } catch (Exception e) {
            statusLabel.setText("Error (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startExam(String subject) {
        try {
            List<Question> questions = ExcelReader.readQuestions(EXCEL_PATH, subject);
            if (questions.isEmpty()) {
                statusLabel.setText("No hay preguntas para " + subject);
                return;
            }
            Collections.shuffle(questions);
            int numQuestions = Math.min(NUM_QUESTIONS, questions.size());
            List<Question> examQuestions = questions.subList(0, numQuestions);
            mainApp.showExam(examQuestions, subject, TIME_MINUTES);
        } catch (Exception e) {
            statusLabel.setText("Error (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            mainApp.showMenu();
        } catch (Exception e) {
            statusLabel.setText("Error (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

}
