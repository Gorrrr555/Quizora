package gor.alaverdyan.myapplication;

import java.util.Random;

public class QuizProvider {
    public static class Question {
        public String text;
        public String[] answers;
        public int correctAnswerIndex;

        public Question(String text, String[] answers, int correct) {
            this.text = text; this.answers = answers; this.correctAnswerIndex = correct;
        }
    }

    public static Question getQuestion(String category, String subCategory, int difficulty) {
        Random r = new Random();
        if (category.equals("Math")) {
            int a = r.nextInt(10 * difficulty) + 1;
            int b = r.nextInt(10 * difficulty) + 1;
            return new Question(a + " + " + b + " = ?",
                    new String[]{String.valueOf(a+b), String.valueOf(a+b+1), String.valueOf(a+b-2)}, 0);
        }
        return new Question("Вопрос по теме " + subCategory, new String[]{"Ответ 1", "Ответ 2", "Ответ 3"}, 0);
    }
}