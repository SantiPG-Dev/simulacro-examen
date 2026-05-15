package com.examenes.controller;

import com.examenes.MainApp;
import com.examenes.model.AnsweredQuestion;
import com.examenes.model.ExamResult;
import com.examenes.model.Question;
import com.examenes.service.HistoryManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ExamController implements Initializable {

    private static final int PHASE_EXAM = 0;
    private static final int PHASE_RESULTS = 1;
    private static final int PHASE_REVIEW = 2;

    @FXML private Label timerLabel;
    @FXML private Label progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label questionLabel;
    @FXML private ToggleGroup optionsGroup;
    @FXML private RadioButton optA;
    @FXML private RadioButton optB;
    @FXML private RadioButton optC;
    @FXML private RadioButton optD;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button finishButton;
    @FXML private VBox reviewOverlay;
    @FXML private Label resultLabel;
    @FXML private VBox questionContent;
    @FXML private Label timerCaption;
    @FXML private Label feedbackLabel;

    private MainApp mainApp;
    private List<Question> questions;
    private int[] userAnswers;
    private boolean[] questionChecked;
    private int currentIndex;
    private int phase;
    private int remainingSeconds;
    private int totalSeconds;
    private Timeline timer;
    private String subject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void initExam(List<Question> questions, String subject, int timeMinutes) {
        this.questions = questions;
        this.subject = subject;
        this.totalSeconds = timeMinutes * 60;
        this.remainingSeconds = totalSeconds;
        this.userAnswers = new int[questions.size()];
        this.questionChecked = new boolean[questions.size()];
        for (int i = 0; i < userAnswers.length; i++) userAnswers[i] = -1;

        this.currentIndex = 0;
        this.phase = PHASE_EXAM;

        reviewOverlay.setVisible(false);
        reviewOverlay.setManaged(false);
        questionContent.setVisible(true);
        questionContent.setManaged(true);
        timerCaption.setVisible(true);
        timerLabel.setVisible(true);

        nextButton.setText("Comprobar");

        showQuestion(0);
        startTimer();
        updateNavButtons();
    }

    private void showQuestion(int index) {
        currentIndex = index;
        Question q = questions.get(index);

        progressLabel.setText("Pregunta " + (index + 1) + " / " + questions.size());
        progressBar.setProgress((double) (index + 1) / questions.size());
        questionLabel.setText((index + 1) + ". " + q.getQuestion());

        RadioButton[] radios = {optA, optB, optC, optD};
        for (int i = 0; i < 4; i++) {
            radios[i].setText((char) ('A' + i) + ". " + q.getOptions().get(i));
            radios[i].setDisable(questionChecked[index]);
            radios[i].getStyleClass().removeAll("correct-option", "incorrect-option");
        }

        optionsGroup.selectToggle(null);
        if (userAnswers[index] >= 0) {
            selectRadio(userAnswers[index]);
        }

        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);
        nextButton.setText(questionChecked[index] ? "Siguiente" : "Comprobar");

        if (questionChecked[index]) {
            showFeedbackStyles(index);
        }

        updateNavButtons();
    }

    private void showReviewQuestion(int index) {
        currentIndex = index;
        Question q = questions.get(index);
        int selected = userAnswers[index];
        int correctIdx = q.getCorrectIndex();

        progressLabel.setText("Pregunta " + (index + 1) + " / " + questions.size());
        progressBar.setProgress((double) (index + 1) / questions.size());
        questionLabel.setText((index + 1) + ". " + q.getQuestion());

        RadioButton[] radios = {optA, optB, optC, optD};
        for (int i = 0; i < 4; i++) {
            radios[i].setText((char) ('A' + i) + ". " + q.getOptions().get(i));
            radios[i].setDisable(true);
            radios[i].getStyleClass().removeAll("correct-option", "incorrect-option");
            if (i == correctIdx) {
                radios[i].getStyleClass().add("correct-option");
            } else if (selected == i) {
                radios[i].getStyleClass().add("incorrect-option");
            }
        }

        optionsGroup.selectToggle(null);
        if (selected >= 0) {
            selectRadio(selected);
        }

        updateNavButtons();
    }

    private void selectRadio(int index) {
        RadioButton[] radios = {optA, optB, optC, optD};
        if (index >= 0 && index < radios.length) {
            optionsGroup.selectToggle(radios[index]);
        }
    }

    @FXML
    private void handlePrev(ActionEvent event) {
        if (currentIndex > 0) {
            int target = currentIndex - 1;
            if (phase == PHASE_REVIEW) {
                showReviewQuestion(target);
            } else {
                if (!questionChecked[currentIndex]) {
                    saveCurrentAnswer();
                }
                showQuestion(target);
            }
        }
    }

    @FXML
    private void handleNext(ActionEvent event) {
        if (phase == PHASE_REVIEW) {
            if (currentIndex < questions.size() - 1) {
                showReviewQuestion(currentIndex + 1);
            }
        } else {
            if (!questionChecked[currentIndex]) {
                checkCurrentAnswer();
            } else if (currentIndex < questions.size() - 1) {
                showQuestion(currentIndex + 1);
            }
        }
    }

    private void checkCurrentAnswer() {
        saveCurrentAnswer();
        questionChecked[currentIndex] = true;

        RadioButton[] radios = {optA, optB, optC, optD};
        for (RadioButton rb : radios) rb.setDisable(true);

        showFeedbackStyles(currentIndex);

        Question q = questions.get(currentIndex);
        int selected = userAnswers[currentIndex];
        int correctIdx = q.getCorrectIndex();

        if (selected < 0) {
            feedbackLabel.setText("Sin responder. Respuesta correcta: " + (char) ('A' + correctIdx));
        } else if (selected == correctIdx) {
            feedbackLabel.setText("Correcto!");
        } else {
            feedbackLabel.setText("Incorrecto. Respuesta correcta: " + (char) ('A' + correctIdx));
        }
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);

        nextButton.setText(currentIndex < questions.size() - 1 ? "Siguiente" : "Finalizar");
        updateNavButtons();
    }

    private void showFeedbackStyles(int index) {
        Question q = questions.get(index);
        int selected = userAnswers[index];
        int correctIdx = q.getCorrectIndex();
        RadioButton[] radios = {optA, optB, optC, optD};
        for (int i = 0; i < 4; i++) {
            radios[i].getStyleClass().removeAll("correct-option", "incorrect-option");
            if (i == correctIdx) {
                radios[i].getStyleClass().add("correct-option");
            } else if (selected == i) {
                radios[i].getStyleClass().add("incorrect-option");
            }
        }
    }

    private void saveCurrentAnswer() {
        Toggle selected = optionsGroup.getSelectedToggle();
        if (selected != null) {
            userAnswers[currentIndex] = optionsGroup.getToggles().indexOf(selected);
        }
    }

    @FXML
    private void handleFinish(ActionEvent event) {
        switch (phase) {
            case PHASE_EXAM -> confirmFinishExam();
            case PHASE_RESULTS -> enterReviewMode();
            case PHASE_REVIEW -> backToMenu();
        }
    }

    private void confirmFinishExam() {
        if (!questionChecked[currentIndex]) {
            checkCurrentAnswer();
        }

        int unanswered = 0;
        for (int ans : userAnswers) {
            if (ans < 0) unanswered++;
        }

        String msg = "Quedan " + unanswered + " preguntas sin responder.\n"
                + "Respondidas: " + (questions.size() - unanswered) + "/" + questions.size()
                + "\n\nFinalizar el simulacro?";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Finalizar Simulacro");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            finishExam();
        }
    }

    private void finishExam() {
        if (timer != null) timer.stop();

        int correct = 0, incorrect = 0, blank = 0;
        List<AnsweredQuestion> answeredList = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            int selected = userAnswers[i];
            AnsweredQuestion aq = new AnsweredQuestion(questions.get(i), selected);
            answeredList.add(aq);
            if (selected < 0) blank++;
            else if (aq.isCorrect()) correct++;
            else incorrect++;
        }

        int timeSpent = Math.min(totalSeconds, totalSeconds - remainingSeconds);

        ExamResult examResult = new ExamResult(
                subject, questions.size(), correct, incorrect, blank, timeSpent, answeredList);

        try {
            HistoryManager.saveResult(examResult);
        } catch (Exception e) {
            showAlert("Error", "No se pudo guardar el resultado: " + e.getMessage());
        }

        showResultScreen(correct, incorrect, blank, timeSpent);
    }

    private void showResultScreen(int correct, int incorrect, int blank, int timeSpent) {
        int total = questions.size();
        double score = (double) correct / total * 10;
        String min = String.format("%02d", timeSpent / 60);
        String sec = String.format("%02d", timeSpent % 60);

        resultLabel.setText(
                "RESULTADO DEL SIMULACRO\n\n"
                + "Aciertos:       " + correct + " / " + total + "\n"
                + "Fallos:         " + incorrect + "\n"
                + "Sin responder:  " + blank + "\n"
                + "Nota:           " + String.format("%.1f", score) + "\n"
                + "Tiempo:         " + min + ":" + sec
        );

        questionContent.setVisible(false);
        questionContent.setManaged(false);
        reviewOverlay.setVisible(true);
        reviewOverlay.setManaged(true);
        timerCaption.setVisible(false);
        timerLabel.setVisible(false);

        phase = PHASE_RESULTS;
        finishButton.setText("Repasar respuestas");
        prevButton.setVisible(false);
        nextButton.setVisible(false);
    }

    private void enterReviewMode() {
        questionContent.setVisible(true);
        questionContent.setManaged(true);
        reviewOverlay.setVisible(false);
        reviewOverlay.setManaged(false);

        prevButton.setVisible(true);
        nextButton.setVisible(true);
        finishButton.setText("Volver al Menu");

        phase = PHASE_REVIEW;
        currentIndex = 0;
        showReviewQuestion(0);
    }

    private void updateNavButtons() {
        if (phase == PHASE_RESULTS) {
            prevButton.setVisible(false);
            nextButton.setVisible(false);
            return;
        }
        prevButton.setDisable(currentIndex == 0);
        nextButton.setDisable(currentIndex == questions.size() - 1);
        prevButton.setVisible(true);
        nextButton.setVisible(true);
    }

    private void startTimer() {
        updateTimerDisplay();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            updateTimerDisplay();
            if (remainingSeconds <= 0) {
                timer.stop();
                saveCurrentAnswer();
                showAlert("Tiempo agotado", "Se ha acabado el tiempo del simulacro.");
                finishExam();
            }
        }));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
    }

    private void updateTimerDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

        timerLabel.getStyleClass().remove("timer-warning");
        if (remainingSeconds < 300) {
            timerLabel.getStyleClass().add("timer-warning");
        }
    }

    private void backToMenu() {
        if (timer != null) timer.stop();
        try {
            mainApp.showMenu();
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
