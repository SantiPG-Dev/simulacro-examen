package com.examenes.controller;

import com.examenes.MainApp;
import com.examenes.model.Question;
import com.examenes.service.ConfigService;
import com.examenes.service.ExcelReader;
import com.examenes.service.GitHubService;
import com.examenes.service.HtmlParser;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ImportMenuController implements Initializable {

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

    @FXML
    private void handleBack() {
        try {
            mainApp.showMenu();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleImportHtml(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Seleccionar carpeta con archivos HTML");
        String htmlFolder = mainApp.getConfigService().getHtmlFolder();
        File htmlDir = new File(htmlFolder);
        if (htmlDir.exists()) {
            dc.setInitialDirectory(htmlDir.getAbsoluteFile());
        }
        File selectedDir = dc.showDialog(null);
        if (selectedDir == null) return;

        try {
            List<File> htmlFiles = new ArrayList<>();
            collectHtmlFiles(selectedDir, htmlFiles);

            if (htmlFiles.isEmpty()) {
                statusLabel.setText("No se encontraron archivos HTML en la carpeta seleccionada.");
                return;
            }

            List<Question> all = new ArrayList<>();
            int parsedFiles = 0;
            for (File f : htmlFiles) {
                try {
                    List<Question> parsed = HtmlParser.parse(f);
                    if (!parsed.isEmpty()) {
                        all.addAll(parsed);
                        parsedFiles++;
                    }
                } catch (Exception ignored) {
                    // saltamos archivos que no se pueden parsear
                }
            }

            if (all.isEmpty()) {
                statusLabel.setText("No se encontraron preguntas en los archivos HTML.");
                return;
            }

            Map<String, List<Question>> bySubject = all.stream()
                    .collect(Collectors.groupingBy(Question::getSubject));

            int totalAdded = 0;
            for (var entry : bySubject.entrySet()) {
                ExcelReader.appendQuestions(getExcelPath(), entry.getKey(), entry.getValue());
                totalAdded += entry.getValue().size();
            }

            statusLabel.setText("Importadas " + totalAdded + " preguntas de " + parsedFiles + " archivos (" + bySubject.size() + " asignaturas).");
        } catch (Exception e) {
            statusLabel.setText("Error al importar: " + e.getMessage());
        }
    }

    /** Recoge recursivamente todos los .html/.htm del directorio */
    private static void collectHtmlFiles(File dir, List<File> result) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                collectHtmlFiles(f, result);
            } else if (f.getName().toLowerCase().endsWith(".html")
                    || f.getName().toLowerCase().endsWith(".htm")) {
                result.add(f);
            }
        }
    }

    @FXML
    private void handleImportExcel(ActionEvent event) {
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
                ExcelReader.appendQuestions(getExcelPath(), entry.getKey(), entry.getValue());
                totalAdded += entry.getValue().size();
            }

            statusLabel.setText("Importadas " + totalAdded + " preguntas desde Excel.");
        } catch (Exception e) {
            statusLabel.setText("Error al importar: " + e.getMessage());
        }
    }

    @FXML
    private void handleImportFromGitHub(ActionEvent event) {
        ConfigService config = mainApp.getConfigService();
        String repo = config.getGithubRepo();
        String token = config.getGithubToken();

        if (repo == null || repo.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Repo no configurado");
            alert.setHeaderText(null);
            alert.setContentText("Ve a Configuracion y establece el repositorio de GitHub (usuario/repo).");
            alert.showAndWait();
            return;
        }

        TextInputDialog folderDialog = new TextInputDialog("html");
        folderDialog.setTitle("Importar desde GitHub");
        folderDialog.setHeaderText("Repositorio: " + repo);
        folderDialog.setContentText("Carpeta dentro del repo (dejar 'html' por defecto):");
        Optional<String> folderResult = folderDialog.showAndWait();
        if (folderResult.isEmpty()) return;
        String folder = folderResult.get().trim();
        if (folder.isEmpty()) folder = "html";
        final String finalFolder = folder;

        TextInputDialog branchDialog = new TextInputDialog("main");
        branchDialog.setTitle("Importar desde GitHub");
        branchDialog.setHeaderText(null);
        branchDialog.setContentText("Rama del repositorio:");
        Optional<String> branchResult = branchDialog.showAndWait();
        if (branchResult.isEmpty()) return;
        String branch = branchResult.get().trim();
        if (branch.isEmpty()) branch = "main";
        final String finalBranch = branch;

        final String finalToken = token;

        statusLabel.setText("Descargando preguntas desde GitHub...");

        Task<List<Question>> task = new Task<>() {
            @Override
            protected List<Question> call() throws Exception {
                GitHubService gh = new GitHubService();
                return gh.importHtmlFromRepo(repo, finalBranch, finalFolder, finalToken);
            }
        };

        task.setOnSucceeded(e -> {
            try {
                List<Question> all = task.getValue();
                if (all.isEmpty()) {
                    statusLabel.setText("No se encontraron preguntas en el repo.");
                    return;
                }

                Map<String, List<Question>> bySubject = all.stream()
                        .collect(Collectors.groupingBy(Question::getSubject));

                String excelPath = getExcelPath();
                int totalAdded = 0;
                for (var entry : bySubject.entrySet()) {
                    ExcelReader.appendQuestions(excelPath, entry.getKey(), entry.getValue());
                    totalAdded += entry.getValue().size();
                }
                statusLabel.setText("Importadas " + totalAdded + " preguntas desde GitHub (" + bySubject.size() + " asignaturas).");
            } catch (Exception ex) {
                statusLabel.setText("Error al guardar: " + ex.getMessage());
            }
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            String msg = ex != null ? ex.getMessage() : "Error desconocido";
            statusLabel.setText("Error de GitHub: " + msg);
        });

        new Thread(task).start();
    }

}
