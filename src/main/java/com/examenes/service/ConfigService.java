package com.examenes.service;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class ConfigService {

    private static final String CONFIG_FILE = "config.properties";
    private static final Path CONFIG_PATH = Paths.get(System.getProperty("user.home"), ".simulacro-examen", CONFIG_FILE);

    private static final String EXCEL_PATH = "excel" + File.separator + "BateriaPreguntas.xlsx";

    private final Properties props;

    public ConfigService() {
        props = new Properties();
        load();
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
        return EXCEL_PATH;
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
        return get("html.folder", "html");
    }

    public void setHtmlFolder(String folder) {
        set("html.folder", folder);
    }

    public String getDownloadsFolder() {
        return get("downloads.folder", "");
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
