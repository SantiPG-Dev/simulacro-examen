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
        ConfigService config = mainApp.getConfigService();
        String htmlFolder = config.getHtmlFolder();
        FileChooser fc = new FileChooser();
        fc.setTitle("Importar preguntas desde HTML");
        File htmlDir = new File(htmlFolder);
        if (htmlDir.exists()) {
            fc.setInitialDirectory(htmlDir.getAbsoluteFile());
        }
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
                ExcelReader.appendQuestions(getExcelPath(), entry.getKey(), entry.getValue());
                totalAdded += entry.getValue().size();
            }

            statusLabel.setText("Importadas " + totalAdded + " preguntas de " + bySubject.size() + " asignaturas.");
        } catch (Exception e) {
            statusLabel.setText("Error al importar: " + e.getMessage());
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
