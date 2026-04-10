package gor.alaverdyan.myapplication;

public class Question {
    private String q, o1, o2, o3, o4;
    private int a;

    public Question(String q, String o1, String o2, String o3, String o4, int a) {
        this.q = q; this.o1 = o1; this.o2 = o2; this.o3 = o3; this.o4 = o4; this.a = a;
    }

    public String getQuestionText() { return q; }
    public String getOption1() { return o1; }
    public String getOption2() { return o2; }
    public String getOption3() { return o3; }
    public String getOption4() { return o4; }
    public int getAnswer() { return a; }
}
