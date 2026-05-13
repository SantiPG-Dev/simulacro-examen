package com.examenes.service;

import com.examenes.model.Question;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestHarness {

    private static final String XLSX_PATH = "excel/BateriaPreguntas.xlsx";
    private static final String HTML_DIR = "html/acceso a datos";

    public static void main(String[] args) throws IOException {
        System.out.println("=== Test Harness: HtmlParser + ExcelReader ===\n");

        File xlsx = new File(XLSX_PATH);
        if (xlsx.exists()) {
            xlsx.delete();
            System.out.println("Deleted existing " + XLSX_PATH);
        }

        // --- Test 1: Normal import from HTML ---
        System.out.println("--- Test 1: Import HTML files ---");
        File htmlDir = new File(HTML_DIR);
        File[] htmlFiles = htmlDir.listFiles((d, name) -> name.endsWith(".htm"));

        int totalParsed = 0;
        for (File f : htmlFiles) {
            List<Question> questions = HtmlParser.parse(f);
            System.out.println("Parsed " + questions.size() + " questions from: " + f.getName());
            totalParsed += questions.size();
            if (!questions.isEmpty()) {
                String subject = questions.get(0).getSubject();
                ExcelReader.appendQuestions(XLSX_PATH, subject, questions);
                System.out.println("Appended " + questions.size() + " questions to sheet '" + subject + "'");
            }
        }
        System.out.println("Total parsed: " + totalParsed);
        printSummary();
        System.out.println();

        // --- Test 2: Re-append same HTML → zero new duplicates ---
        System.out.println("--- Test 2: Re-append (should add 0) ---");
        for (File f : htmlFiles) {
            List<Question> questions = HtmlParser.parse(f);
            if (!questions.isEmpty()) {
                String subject = questions.get(0).getSubject();
                int before = ExcelReader.readQuestions(XLSX_PATH, subject).size();
                ExcelReader.appendQuestions(XLSX_PATH, subject, questions);
                int after = ExcelReader.readQuestions(XLSX_PATH, subject).size();
                System.out.println("  " + subject + " (from " + f.getName() + "): " + before + " -> " + after);
                System.out.println(before == after ? "  PASS" : "  FAIL: duplicate added!");
            }
        }
        System.out.println();

        // --- Test 3: Manually inject duplicates, then append → dedup should clean them ---
        System.out.println("--- Test 3: Inject duplicates then append ---");
        String testSubject = "ACCESO A DATOS";
        List<Question> existing = ExcelReader.readQuestions(XLSX_PATH, testSubject);
        if (existing.size() >= 2) {
            Question dup1 = existing.get(0);
            Question dup2 = existing.get(1);
            List<Question> inject = new ArrayList<>();
            inject.add(dup1);
            inject.add(dup2);

            ExcelReader.appendQuestions(XLSX_PATH, testSubject, inject);
            int afterInject = ExcelReader.readQuestions(XLSX_PATH, testSubject).size();
            System.out.println("  After injecting 2 duplicates: " + afterInject + " (expected " + existing.size() + ")");
            System.out.println(afterInject == existing.size() ? "  PASS: duplicates removed" : "  FAIL: count mismatch");
        }
        System.out.println();

        // --- Test 4: Append new unique question ---
        System.out.println("--- Test 4: Append new unique question ---");
        List<String> opts = List.of("Option A", "Option B", "Option C");
        Question newQ = new Question(testSubject, "Pregunta de prueba unica?", opts, 0);
        int beforeNew = ExcelReader.readQuestions(XLSX_PATH, testSubject).size();
        ExcelReader.appendQuestions(XLSX_PATH, testSubject, List.of(newQ));
        int afterNew = ExcelReader.readQuestions(XLSX_PATH, testSubject).size();
        System.out.println("  Before: " + beforeNew + ", After: " + afterNew + " (expected " + (beforeNew + 1) + ")");
        System.out.println(afterNew == beforeNew + 1 ? "  PASS" : "  FAIL");
        System.out.println();

        // --- Test 5: Append same new question again → should be skipped ---
        System.out.println("--- Test 5: Re-append same new question (should skip) ---");
        ExcelReader.appendQuestions(XLSX_PATH, testSubject, List.of(newQ));
        int afterReDup = ExcelReader.readQuestions(XLSX_PATH, testSubject).size();
        System.out.println("  After re-appending: " + afterReDup + " (expected " + (beforeNew + 1) + ")");
        System.out.println(afterReDup == beforeNew + 1 ? "  PASS" : "  FAIL");
        System.out.println();

        System.out.println("=== Final summary ===");
        printSummary();
        System.out.println("\n=== Test harness completed ===");
    }

    private static void printSummary() throws IOException {
        System.out.println("  Total questions: " + ExcelReader.readQuestions(XLSX_PATH).size());
        for (String s : ExcelReader.getSubjects(XLSX_PATH)) {
            int count = ExcelReader.readQuestions(XLSX_PATH, s).size();
            System.out.println("    " + s + ": " + count + " questions");
        }
    }
}
