package com.examenes.service;

import com.examenes.model.ExamResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private static final Path DATA_DIR = Paths.get(
            System.getProperty("user.home"), ".simulacro-examen");
    private static final Path HISTORY_FILE = DATA_DIR.resolve("history.json");
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    public static void saveResult(ExamResult result) throws IOException {
        List<ExamResult> history = loadHistory();
        history.add(result);
        Files.createDirectories(DATA_DIR);

        String json = GSON.toJson(history);
        Files.writeString(HISTORY_FILE, json);
    }

    public static List<ExamResult> loadHistory() throws IOException {
        if (!Files.exists(HISTORY_FILE)) {
            return new ArrayList<>();
        }

        String json = Files.readString(HISTORY_FILE);
        Type listType = new TypeToken<List<ExamResult>>() {}.getType();
        List<ExamResult> history = GSON.fromJson(json, listType);
        return history != null ? history : new ArrayList<>();
    }

    public static void clearHistory() throws IOException {
        Files.createDirectories(DATA_DIR);
        Files.writeString(HISTORY_FILE, "[]");
    }

}
