package com.examenes.service;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class ConfigService {

    private static final String CONFIG_FILE = "config.properties";
    private static final Path CONFIG_PATH = Paths.get(System.getProperty("user.home"), ".simulacro-examen", CONFIG_FILE);

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
            e.printStackTrace();
        }
    }

    public String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public void set(String key, String value) {
        props.setProperty(key, value);
    }

    public String getExcelPath() {
        return get("excel.path", "excel/BateriaPreguntas.xlsx");
    }

    public void setExcelPath(String path) {
        set("excel.path", path);
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

}
