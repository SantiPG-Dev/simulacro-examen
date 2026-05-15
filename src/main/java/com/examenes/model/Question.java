package com.examenes.model;

import java.util.List;

public class Question {

    private String subject;       // Asignatura a la que pertenece la pregunta
    private String question;      // Texto del enunciado
    private List<String> options; // Lista de opciones de respuesta
    private int correctIndex;     // Indice de la opcion correcta (0=A, 1=B, ...)

    public Question() {
    }

    // Constructor que inicializa todos los campos de la pregunta
    public Question(String subject, String question, List<String> options, int correctIndex) {
        this.subject = subject;
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

}
