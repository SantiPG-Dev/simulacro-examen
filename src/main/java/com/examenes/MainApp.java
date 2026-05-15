package com.examenes;

import com.examenes.controller.ExamController;
import com.examenes.controller.ConfigController;
import com.examenes.controller.DataMenuController;
import com.examenes.controller.HistoryController;
import com.examenes.controller.ImportMenuController;
import com.examenes.controller.MenuController;
import com.examenes.controller.SubjectSelectController;
import com.examenes.model.Question;
import com.examenes.service.ConfigService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class MainApp extends Application {

    private Stage primaryStage;
    private ConfigService configService;
    private boolean isDarkTheme;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        configService = new ConfigService();
        isDarkTheme = configService.isDarkTheme();
        showMenu();
        stage.setTitle("Simulacros de Examen");
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

    public void showConfig() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/config.fxml"));
        VBox root = loader.load();
        ConfigController controller = loader.getController();
        controller.setMainApp(this);
        setScene(root);
    }

    public void showImportMenu() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/import-menu.fxml"));
        VBox root = loader.load();
        ImportMenuController controller = loader.getController();
        controller.setMainApp(this);
        setScene(root);
    }

    public void showDataMenu() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/data-menu.fxml"));
        VBox root = loader.load();
        DataMenuController controller = loader.getController();
        controller.setMainApp(this);
        setScene(root);
    }

    public void showSubjectSelect() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/subject-select.fxml"));
        VBox root = loader.load();
        SubjectSelectController controller = loader.getController();
        controller.setMainApp(this);
        controller.loadSubjects();
        setScene(root);
    }

    private void setScene(VBox root) {
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        if (isDarkTheme) {
            scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
        }
        primaryStage.setScene(scene);
    }

    public boolean isDarkTheme() {
        return isDarkTheme;
    }

    public void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        configService.setTheme(isDarkTheme ? "dark" : "light");
        configService.save();
        refreshCurrentScene();
    }

    private void refreshCurrentScene() {
        Scene scene = primaryStage.getScene();
        if (scene == null) return;
        String darkCss = getClass().getResource("/dark-theme.css").toExternalForm();
        if (isDarkTheme) {
            if (!scene.getStylesheets().contains(darkCss)) {
                scene.getStylesheets().add(darkCss);
            }
        } else {
            scene.getStylesheets().remove(darkCss);
        }
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
