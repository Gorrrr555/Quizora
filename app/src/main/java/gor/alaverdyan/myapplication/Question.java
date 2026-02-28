package gor.alaverdyan.myapplication;

public class Question {
    public String text;
    public String[] options;
    public int correct;

    public Question(String text, String[] options, int correct) {
        this.text = text;
        this.options = options;
        this.correct = correct;
    }
}