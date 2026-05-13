package com.examenes.service;

import com.examenes.model.Question;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HtmlParser {

    public static List<Question> parse(File htmlFile) throws IOException {
        Document doc = Jsoup.parse(htmlFile, "UTF-8");
        String subject = extractSubject(doc.title());
        List<Question> questions = new ArrayList<>();

        Elements questionBlocks = doc.select(".que.multichoice");
        for (Element block : questionBlocks) {
            String qtext = block.selectFirst("div.qtext .clearfix") != null
                    ? block.selectFirst("div.qtext .clearfix").wholeText().trim()
                    : "";

            if (qtext.isEmpty()) continue;

            List<String> options = new ArrayList<>();
            Elements optionElements = block.select("div.answer > div.r0, div.answer > div.r1");
            for (Element optEl : optionElements) {
                Element textEl = optEl.selectFirst("div.flex-fill.ms-1");
                if (textEl != null) {
                    options.add(textEl.wholeText().trim());
                }
            }

            if (options.isEmpty()) continue;

            Element rightAnswerEl = block.selectFirst("div.rightanswer");
            String correctText = "";
            if (rightAnswerEl != null) {
                correctText = rightAnswerEl.wholeText().trim();
                String prefix = "La respuesta correcta es: ";
                if (correctText.startsWith(prefix)) {
                    correctText = correctText.substring(prefix.length()).trim();
                }
            }

            int correctIndex = -1;
            if (!correctText.isEmpty()) {
                String normalized = normalize(correctText);
                for (int i = 0; i < options.size(); i++) {
                    if (normalize(options.get(i)).equals(normalized)) {
                        correctIndex = i;
                        break;
                    }
                }
            }

            if (correctIndex < 0) continue;

            questions.add(new Question(subject, qtext, options, correctIndex));
        }

        return questions;
    }

    static String extractSubject(String title) {
        String s = title;
        if (s.toUpperCase().startsWith("SIMULACRO ")) {
            s = s.substring("SIMULACRO ".length());
        }
        int colonIdx = s.indexOf(':');
        if (colonIdx >= 0) {
            s = s.substring(0, colonIdx);
        }
        s = s.replaceAll("\\(.*?\\)", "").trim();
        String[] words = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0)));
            sb.append(w.substring(1).toLowerCase());
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    private static String normalize(String s) {
        String r = s.strip();
        if (r.endsWith(".")) r = r.substring(0, r.length() - 1).strip();
        return r;
    }
}
