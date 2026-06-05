package gor.alaverdyan.myapplication;

public class Question {
    private String text;
    private String[] options;
    private int correctIndex;
    private String explanation;

    public Question(String text, String[] options, int correctIndex, String explanation) {
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }

    public String getText() { return text; }
    public String[] getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
    public String getExplanation() { return explanation; }
}
