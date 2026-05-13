package com.examenes.controller;

import com.examenes.MainApp;
import com.examenes.model.ExamResult;
import com.examenes.service.HistoryManager;
import com.examenes.util.ThemeToggle;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class HistoryController implements Initializable {

    @FXML private TableView<ExamResultRow> historyTable;
    @FXML private TableColumn<ExamResultRow, String> dateCol;
    @FXML private TableColumn<ExamResultRow, String> subjectCol;
    @FXML private TableColumn<ExamResultRow, Integer> correctCol;
    @FXML private TableColumn<ExamResultRow, Integer> incorrectCol;
    @FXML private TableColumn<ExamResultRow, Integer> blankCol;
    @FXML private TableColumn<ExamResultRow, Double> scoreCol;
    @FXML private Label summaryLabel;
    @FXML private Button clearButton;
    @FXML private ThemeToggle themeToggle;

    private MainApp mainApp;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        subjectCol.setCellValueFactory(cellData -> cellData.getValue().subjectProperty());
        correctCol.setCellValueFactory(cellData -> cellData.getValue().correctProperty().asObject());
        incorrectCol.setCellValueFactory(cellData -> cellData.getValue().incorrectProperty().asObject());
        blankCol.setCellValueFactory(cellData -> cellData.getValue().blankProperty().asObject());
        scoreCol.setCellValueFactory(cellData -> cellData.getValue().scoreProperty().asObject());

        scoreCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", item));
                    if (item >= 70) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (item >= 50) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        loadHistory();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        themeToggle.setDarkMode(mainApp.isDarkTheme());
        themeToggle.setOnToggle(() -> mainApp.toggleTheme());
    }

    private void loadHistory() {
        try {
            List<ExamResult> results = HistoryManager.loadHistory();
            ObservableList<ExamResultRow> rows = FXCollections.observableArrayList();

            double totalScore = 0;
            int totalCorrect = 0;
            int totalQuestions = 0;

            for (ExamResult r : results) {
                rows.add(new ExamResultRow(r));
                totalScore += r.getScore();
                totalCorrect += r.getCorrectAnswers();
                totalQuestions += r.getTotalQuestions();
            }

            historyTable.setItems(rows);

            int count = results.size();
            if (count > 0) {
                double avg = totalScore / count;
                double pct = totalQuestions > 0 ? (double) totalCorrect / totalQuestions * 100 : 0;
                summaryLabel.setText(
                        "Simulacros: " + count
                        + " | Media: " + String.format("%.1f%%", avg)
                        + " | Global: " + String.format("%.1f%%", pct)
                );
            } else {
                summaryLabel.setText("No hay resultados guardados.");
                clearButton.setDisable(true);
            }

        } catch (Exception e) {
            summaryLabel.setText("Error al cargar historial: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Limpiar Historial");
        alert.setHeaderText(null);
        alert.setContentText("Seguro que quieres borrar todo el historial?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        var result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                HistoryManager.clearHistory();
                loadHistory();
            } catch (Exception e) {
                showAlert("Error", "No se pudo limpiar el historial: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            mainApp.showMenu();
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class ExamResultRow {
        private final SimpleStringProperty date;
        private final SimpleStringProperty subject;
        private final SimpleIntegerProperty correct;
        private final SimpleIntegerProperty incorrect;
        private final SimpleIntegerProperty blank;
        private final SimpleDoubleProperty score;

        public ExamResultRow(ExamResult r) {
            this.date = new SimpleStringProperty(
                    r.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            this.subject = new SimpleStringProperty(r.getSubject());
            this.correct = new SimpleIntegerProperty(r.getCorrectAnswers());
            this.incorrect = new SimpleIntegerProperty(r.getIncorrectAnswers());
            this.blank = new SimpleIntegerProperty(r.getBlankAnswers());
            this.score = new SimpleDoubleProperty(r.getScore());
        }

        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty subjectProperty() { return subject; }
        public SimpleIntegerProperty correctProperty() { return correct; }
        public SimpleIntegerProperty incorrectProperty() { return incorrect; }
        public SimpleIntegerProperty blankProperty() { return blank; }
        public SimpleDoubleProperty scoreProperty() { return score; }
    }

}
