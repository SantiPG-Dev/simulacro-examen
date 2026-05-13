package com.examenes;

import com.examenes.controller.ExamController;
import com.examenes.controller.HistoryController;
import com.examenes.controller.MenuController;
import com.examenes.model.Question;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        showMenu();
        stage.setTitle("Simulacro de Examen");
        stage.setResizable(false);
        stage.show();
    }

    public void showMenu() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
        VBox root = loader.load();
        MenuController controller = loader.getController();
        controller.setMainApp(this);
        setScene(root);
    }

    public void showExam(List<Question> questions, String subject, int timeMinutes) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exam.fxml"));
        VBox root = loader.load();
        ExamController controller = loader.getController();
        controller.setMainApp(this);
        controller.initExam(questions, subject, timeMinutes);
        setScene(root);
    }

    public void showHistory() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/history.fxml"));
        VBox root = loader.load();
        HistoryController controller = loader.getController();
        controller.setMainApp(this);
        setScene(root);
    }

    private void setScene(VBox root) {
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
