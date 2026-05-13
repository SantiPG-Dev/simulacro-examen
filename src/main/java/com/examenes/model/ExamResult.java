package com.examenes.model;

import java.time.LocalDateTime;
import java.util.List;

public class ExamResult {

    private LocalDateTime date;
    private String subject;
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private int blankAnswers;
    private double score;
    private int timeSpentSeconds;
    private List<AnsweredQuestion> answeredQuestions;

    public ExamResult() {
    }

    public ExamResult(String subject, int totalQuestions, int correctAnswers,
                      int incorrectAnswers, int blankAnswers, int timeSpentSeconds,
                      List<AnsweredQuestion> answeredQuestions) {
        this.date = LocalDateTime.now();
        this.subject = subject;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = incorrectAnswers;
        this.blankAnswers = blankAnswers;
        this.timeSpentSeconds = timeSpentSeconds;
        this.answeredQuestions = answeredQuestions;
        this.score = totalQuestions > 0
                ? (double) correctAnswers / totalQuestions * 100
                : 0;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(int incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public int getBlankAnswers() {
        return blankAnswers;
    }

    public void setBlankAnswers(int blankAnswers) {
        this.blankAnswers = blankAnswers;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

    public void setTimeSpentSeconds(int timeSpentSeconds) {
        this.timeSpentSeconds = timeSpentSeconds;
    }

    public List<AnsweredQuestion> getAnsweredQuestions() {
        return answeredQuestions;
    }

    public void setAnsweredQuestions(List<AnsweredQuestion> answeredQuestions) {
        this.answeredQuestions = answeredQuestions;
    }

}
