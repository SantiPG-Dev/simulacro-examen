package com.examenes.service;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class ConfigService {

    private static final String CONFIG_FILE = "config.properties";
    private static final Path CONFIG_PATH = Paths.get(System.getProperty("user.home"), ".simulacro-examen", CONFIG_FILE);

    private static final String EXCEL_PATH = "excel" + File.separator + "BateriaPreguntas.xlsx";

    private final Properties props;
    private final String appDir;

    public ConfigService() {
        props = new Properties();
        appDir = detectAppDir();
        load();
    }

    /** Detecta el directorio de instalacion de la app */
    private static String detectAppDir() {
        try {
            java.net.URI uri = ConfigService.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI();
            File codeSource = new File(uri);
            if (codeSource.isFile()) {
                // JAR -> parent/app/ es "app", abuelo es la raiz de instalacion
                File parent = codeSource.getParentFile();
                if (parent != null && "app".equalsIgnoreCase(parent.getName())) {
                    parent = parent.getParentFile();
                }
                if (parent != null) return parent.getAbsolutePath();
            }
        } catch (Exception ignored) {}
        return System.getProperty("user.dir");
    }

    private void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                try (InputStream is = Files.newInputStream(CONFIG_PATH)) {
                    props.load(is);
                }
            }
        } catch (IOException e) {
            props.clear();
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream os = Files.newOutputStream(CONFIG_PATH)) {
                props.store(os, "Simulacro Examen Config");
            }
        } catch (IOException e) {
            System.err.println("Error guardando configuracion: " + e.getMessage());
        }
    }

    public String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public void set(String key, String value) {
        props.setProperty(key, value);
    }

    public String getExcelPath() {
        return new File(appDir, EXCEL_PATH).getAbsolutePath();
    }

    public String getGithubRepo() {
        return get("github.repo", "");
    }

    public void setGithubRepo(String repo) {
        set("github.repo", repo);
    }

    public String getGithubToken() {
        return get("github.token", "");
    }

    public void setGithubToken(String token) {
        set("github.token", token);
    }

    public String getHtmlFolder() {
        return get("html.folder", new File(appDir, "html").getAbsolutePath());
    }

    public void setHtmlFolder(String folder) {
        set("html.folder", folder);
    }

    public String getDownloadsFolder() {
        String defaultDownloads = System.getProperty("user.home") + File.separator + "Downloads";
        return get("downloads.folder", defaultDownloads);
    }

    public void setDownloadsFolder(String folder) {
        set("downloads.folder", folder);
    }

    public String getTheme() {
        return get("theme", "light");
    }

    public void setTheme(String theme) {
        set("theme", theme);
    }

    public boolean isDarkTheme() {
        return "dark".equals(getTheme());
    }

}
