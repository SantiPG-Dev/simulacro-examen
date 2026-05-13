package com.examenes.model;

import java.util.List;

public class AnsweredQuestion {

    private String question;
    private List<String> options;
    private int correctIndex;
    private int selectedIndex;
    private boolean isCorrect;

    public AnsweredQuestion() {
    }

    public AnsweredQuestion(Question q, int selectedIndex) {
        this.question = q.getQuestion();
        this.options = q.getOptions();
        this.correctIndex = q.getCorrectIndex();
        this.selectedIndex = selectedIndex;
        this.isCorrect = selectedIndex == q.getCorrectIndex();
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public void setCorrectIndex(int correctIndex) {
        this.correctIndex = correctIndex;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

}
