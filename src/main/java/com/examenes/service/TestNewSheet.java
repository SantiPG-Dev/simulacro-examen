package com.examenes.service;

import com.examenes.model.Question;
import java.io.File;
import java.util.List;
import java.util.Map;

public class TestNewSheet {
    private static final String XLSX_PATH = "excel/BateriaPreguntas.xlsx";
    private static final String HTML_DIR = "html/acceso a datos";

    public static void main(String[] args) throws Exception {
        File xlsx = new File(XLSX_PATH);
        // Delete and recreate with sample
        if (xlsx.exists()) xlsx.delete();
        System.out.println("Creating sample Excel...");
        ExcelReader.createSampleExcel(XLSX_PATH);

        // Show existing sheets
        System.out.println("\nBefore import:");
        for (Map.Entry<String, Integer> e : ExcelReader.getSubjectQuestionCounts(XLSX_PATH).entrySet()) {
            System.out.println("  " + e.getKey() + ": " + e.getValue() + " questions");
        }

        // Parse and import a NEW subject
        File htmlDir = new File(HTML_DIR);
        File[] htmlFiles = htmlDir.listFiles((d, name) -> name.endsWith(".htm") || name.endsWith(".html"));
        if (htmlFiles == null || htmlFiles.length == 0) {
            System.out.println("No HTML files found in " + HTML_DIR);
            return;
        }
        File html = htmlFiles[0];
        List<Question> questions = HtmlParser.parse(html);
        System.out.println("\nParsed " + questions.size() + " questions from " + html.getName());
        if (!questions.isEmpty()) {
            String subject = questions.get(0).getSubject();
            System.out.println("Subject from HTML title: '" + subject + "'");

            ExcelReader.appendQuestions(XLSX_PATH, subject, questions);
            System.out.println("Done. Questions appended to sheet '" + subject + "'");
        }

        // Show sheets after import (should now include both sample sheets + new one)
        System.out.println("\nAfter import:");
        for (Map.Entry<String, Integer> e : ExcelReader.getSubjectQuestionCounts(XLSX_PATH).entrySet()) {
            System.out.println("  " + e.getKey() + ": " + e.getValue() + " questions");
        }

        // Verify new sheet was created
        Map<String, Integer> counts = ExcelReader.getSubjectQuestionCounts(XLSX_PATH);
        if (counts.containsKey("ACCESO A DATOS")) {
            System.out.println("\nPASS: 'ACCESO A DATOS' sheet created with " + counts.get("ACCESO A DATOS") + " questions");
        } else {
            System.out.println("\nFAIL: 'ACCESO A DATOS' sheet NOT found");
        }
        if (counts.containsKey("Programacion")) {
            System.out.println("PASS: 'Programacion' sheet still exists with " + counts.get("Programacion") + " questions");
        } else {
            System.out.println("FAIL: 'Programacion' sheet MISSING");
        }
    }
}
