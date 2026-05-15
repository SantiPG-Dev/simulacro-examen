/*
 * Simulacro de Examen
 * Copyright © 2025 @SantiPG-Dev
 *
 * Este software es de libre uso, pero queda PROHIBIDA cualquier
 * modificación del código fuente. Consulta LICENSE para más detalles.
 */
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
        setupUninstaller();
        showMenu();
        stage.setTitle("Simulacros de Examen");
        stage.setResizable(false);
        stage.show();
    }

    /** Instala el desinstalador en el menu de inicio */
    private void setupUninstaller() {
        try {
            // Buscar ProductCode en el registro de Windows
            String productCode = findProductCode();
            if (productCode == null) return;

            String startMenu = System.getProperty("user.home")
                + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Simulacro de Examen";
            new java.io.File(startMenu).mkdirs();

            // Crear acceso directo a msiexec /x {productCode} usando VBScript
            String vbs = "Set WshShell = WScript.CreateObject(\"WScript.Shell\")\n"
                + "Set Shortcut = WshShell.CreateShortcut(\"" + startMenu
                + "\\\\Desinstalar.lnk\")\n"
                + "Shortcut.TargetPath = \"C:\\\\WINDOWS\\\\system32\\\\msiexec.exe\"\n"
                + "Shortcut.Arguments = \"/x {" + productCode + "}\"\n"
                + "Shortcut.WorkingDirectory = \"C:\\\\WINDOWS\\\\system32\"\n"
                + "Shortcut.Description = \"Desinstalar Simulacro de Examen\"\n"
                + "Shortcut.IconLocation = \"%%SystemRoot%%\\\\system32\\\\msiexec.exe,0\"\n"
                + "Shortcut.Save\n";

            java.io.File vbsFile = new java.io.File(
                System.getProperty("java.io.tmpdir"), "mklnk.vbs");
            java.nio.file.Files.writeString(vbsFile.toPath(), vbs);
            Runtime.getRuntime().exec(new String[]{
                "wscript.exe", vbsFile.getAbsolutePath()}).waitFor();
            vbsFile.delete();
        } catch (Exception ignored) {}
    }

    /** Busca el ProductCode en el registro */
    private static String findProductCode() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{
                "reg", "query", "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
                "/s", "/f", "Simulacro de Examen", "/e"
            });
            java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.contains("{") && line.contains("}")) {
                    int s = line.indexOf('{');
                    int e = line.indexOf('}');
                    if (s >= 0 && e > s) {
                        return line.substring(s + 1, e);
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
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
