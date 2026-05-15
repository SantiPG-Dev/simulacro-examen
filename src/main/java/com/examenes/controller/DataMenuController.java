package com.examenes.controller;

import com.examenes.MainApp;
import com.examenes.service.GitHubService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ResourceBundle;

public class DataMenuController implements Initializable {

    @FXML private Label statusLabel;
    private MainApp mainApp;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    // Abro el menu de importacion (HTML, Excel, GitHub)
    private void handleImport(ActionEvent event) {
        try {
            mainApp.showImportMenu();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    // Escaneo la carpeta de descargas y organizo los SIMULACRO*.html por asignatura
    private void handleFixWebs(ActionEvent event) {
        String downloadsPath = mainApp.getConfigService().getDownloadsFolder();
        String htmlBasePath = mainApp.getConfigService().getHtmlFolder();

        if (downloadsPath == null || downloadsPath.isBlank()) {
            statusLabel.setText("Configura la carpeta de descargas en Ajustes primero.");
            return;
        }

        try {
            File downloadsDir = new File(downloadsPath);
            if (!downloadsDir.exists() || !downloadsDir.isDirectory()) {
                statusLabel.setText("La carpeta de descargas no existe: " + downloadsPath);
                return;
            }

            // Filtro solo los archivos que empiezan por SIMULACRO
            File[] simulacroFiles = downloadsDir.listFiles((dir, name) ->
                name.toUpperCase().startsWith("SIMULACRO") &&
                (name.toLowerCase().endsWith(".html") || name.toLowerCase().endsWith(".htm")));
            if (simulacroFiles == null || simulacroFiles.length == 0) {
                statusLabel.setText("No se encontraron archivos SIMULACRO en la carpeta de descargas.");
                return;
            }

            int processed = 0;
            int skipped = 0;
            int renamed = 0;

            for (File f : simulacroFiles) {
                // Extraigo la asignatura del nombre del archivo
                String subject = extractSubjectFromFilename(f.getName());
                if (subject.isEmpty()) {
                    skipped++;
                    continue;
                }

                // Creo la carpeta de la asignatura si no existe
                File subjectDir = new File(htmlBasePath, subject);
                if (!subjectDir.exists()) {
                    subjectDir.mkdirs();
                }

                File destFile = new File(subjectDir, f.getName());
                if (!destFile.exists()) {
                    // Muevo el archivo si no hay conflicto
                    Files.move(f.toPath(), destFile.toPath());
                    processed++;
                } else {
                    // Si el archivo ya existe, comparo fechas para ver si es el mismo
                    long srcModified = f.lastModified() / 1000;
                    long dstModified = destFile.lastModified() / 1000;
                    if (srcModified == dstModified) {
                        skipped++;
                    } else {
                        // Renombro el archivo en descargas con un sufijo (x)
                        int maxX = findMaxSuffix(subjectDir, f.getName());
                        String newName = addSuffix(f.getName(), maxX + 1);
                        File renamedFile = new File(f.getParent(), newName);
                        f.renameTo(renamedFile);
                        renamed++;
                    }
                }
            }

            String msg = "Procesados " + processed + " archivos.";
            if (renamed > 0) msg += " Renombrados " + renamed + " por conflicto.";
            if (skipped > 0) msg += " Saltados " + skipped + " (duplicados).";
            statusLabel.setText(msg);
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleFixWebsGitHub(ActionEvent event) {
        String downloadsPath = mainApp.getConfigService().getDownloadsFolder();
        String repo = mainApp.getConfigService().getGithubRepo();
        String token = mainApp.getConfigService().getGithubToken();

        if (downloadsPath == null || downloadsPath.isBlank()) {
            statusLabel.setText("Configura la carpeta de descargas en Ajustes primero.");
            return;
        }
        if (repo == null || repo.isBlank()) {
            statusLabel.setText("Configura el repositorio GitHub en Ajustes primero.");
            return;
        }

        statusLabel.setText("Subiendo a GitHub...");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                File downloadsDir = new File(downloadsPath);
                if (!downloadsDir.exists() || !downloadsDir.isDirectory()) {
                    throw new Exception("La carpeta de descargas no existe: " + downloadsPath);
                }

                File[] simulacroFiles = downloadsDir.listFiles((dir, name) ->
                    name.toUpperCase().startsWith("SIMULACRO") &&
                    (name.toLowerCase().endsWith(".html") || name.toLowerCase().endsWith(".htm")));
                if (simulacroFiles == null || simulacroFiles.length == 0) {
                    throw new Exception("No se encontraron archivos SIMULACRO en descargas.");
                }

                GitHubService gh = new GitHubService();
                String branch = "main";
                int uploaded = 0, renamed = 0, skipped = 0;

                for (File f : simulacroFiles) {
                    String subject = extractSubjectFromFilename(f.getName());
                    if (subject.isEmpty()) { skipped++; continue; }

                    String ghPath = "html/" + subject + "/" + f.getName();
                    String sha = gh.getFileSha(repo, ghPath, branch, token);

                    if (sha == null) {
                        gh.uploadFile(repo, ghPath,
                            java.nio.file.Files.readAllBytes(f.toPath()),
                            "Añadir " + f.getName(), branch, token, null);
                        uploaded++;
                    } else {
                        // buscar (x) incremental hasta encontrar hueco
                        int x = 2;
                        while (true) {
                            String newName = addSuffix(f.getName(), x);
                            String testPath = "html/" + subject + "/" + newName;
                            if (gh.getFileSha(repo, testPath, branch, token) == null) {
                                gh.uploadFile(repo, testPath,
                                    java.nio.file.Files.readAllBytes(f.toPath()),
                                    "Añadir " + newName, branch, token, null);
                                renamed++;
                                break;
                            }
                            x++;
                        }
                    }
                }
                return "Subidos " + uploaded + " archivos a GitHub."
                     + (renamed > 0 ? " Renombrados " + renamed + " por conflicto." : "")
                     + (skipped > 0 ? " Saltados " + skipped + " (sin asignatura)." : "");
            }
        };

        task.setOnSucceeded(e -> statusLabel.setText(task.getValue()));
        task.setOnFailed(e -> statusLabel.setText("Error GitHub: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    /**
     * Extrae la asignatura del nombre del archivo.
     * "SIMULACRO Acceso a datos (texto).html" -> "Acceso a datos"
     * "SIMULACRO DE Acceso a datos (texto).html" -> "Acceso a datos"
     */
    private static String extractSubjectFromFilename(String name) {
        String s = name.trim();
        int dotIdx = s.lastIndexOf('.');
        if (dotIdx > 0) s = s.substring(0, dotIdx);
        s = s.replaceFirst("^(?i)SIMULACRO\\s+(?:DE\\s+)?", "");
        int parenIdx = s.indexOf('(');
        if (parenIdx >= 0) {
            s = s.substring(0, parenIdx).trim();
        }
        return s.trim();
    }

    /**
     * Busca en el directorio el sufijo (x) más alto para el mismo archivo base.
     * Examina los nombres que coinciden excepto por el (x).
     */
    private static int findMaxSuffix(File dir, String filename) {
        int max = 0;
        String baseNoSuffix = stripSuffix(filename);
        File[] existing = dir.listFiles();
        if (existing == null) return max;
        Pattern p = Pattern.compile(
            Pattern.quote(baseNoSuffix) + " \\((\\d+)\\)\\.html?$", Pattern.CASE_INSENSITIVE);
        for (File ef : existing) {
            Matcher m = p.matcher(ef.getName());
            if (m.matches()) {
                int val = Integer.parseInt(m.group(1));
                if (val > max) max = val;
            }
        }
        return max;
    }

    /**
     * Elimina un sufijo (x) existente del nombre, si lo tiene.
     * "SIMULACRO Tema (test) (2).html" -> "SIMULACRO Tema (test)"
     */
    private static String stripSuffix(String name) {
        return name.replaceFirst(" \\(\\d+\\)(\\.html?)$", "$1");
    }

    /**
     * Añade (x) justo antes de la extensión.
     * "SIMULACRO Tema (test).html" con x=2 -> "SIMULACRO Tema (test) (2).html"
     */
    private static String addSuffix(String name, int x) {
        int dotIdx = name.lastIndexOf('.');
        if (dotIdx > 0) {
            return name.substring(0, dotIdx) + " (" + x + ")" + name.substring(dotIdx);
        }
        return name + " (" + x + ")";
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            mainApp.showMenu();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
}
