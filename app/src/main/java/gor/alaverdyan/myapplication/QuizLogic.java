package gor.alaverdyan.myapplication;

import java.util.Random;

public class QuizLogic {
    private static Random random = new Random();

    public static String generateMathQuestion(int difficulty) {
        int a = random.nextInt(20) + 1;
        int b = random.nextInt(20) + 1;

        if (difficulty == 1) return a + " + " + b + " = ?";
        if (difficulty == 2) return a + " * " + b + " = ?";
        return "Solve: " + a + "x + " + b + " = 0";
    }

    public static String generateHistoryQuestion(String region, int difficulty) {
        if (difficulty == 1) return "Who was the first president of " + region + "?";
        if (difficulty == 2) return "In which year did a major war start in " + region + "?";
        return "Explain an important historical revolution in " + region + ".";
    }

    public static String generateChemistryQuestion(int difficulty) {
        if (difficulty == 1) return "What is the chemical symbol for Oxygen?";
        if (difficulty == 2) return "What is the atomic number of Carbon?";
        return "Balance this equation: H2 + O2 -> H2O";
    }
}