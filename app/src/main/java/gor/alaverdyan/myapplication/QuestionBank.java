package gor.alaverdyan.myapplication;

import java.util.ArrayList;
import java.util.List;

public class QuestionBank {
    public static List<Question> getQuestions(String category, String difficulty) {
        List<Question> list = new ArrayList<>();

        if (category.equals("Math")) {
            if (difficulty.equals("Easy")) {
                list.add(new Question("15 + 7?", new String[]{"21", "22", "23"}, 1));
                list.add(new Question("10 - 4?", new String[]{"6", "5", "7"}, 0));
            } else if (difficulty.equals("Medium")) {
                list.add(new Question("12 * 12?", new String[]{"124", "144", "164"}, 1));
                list.add(new Question("√81?", new String[]{"7", "8", "9"}, 2));
            } else {
                list.add(new Question("Solve: 2x + 10 = 20", new String[]{"5", "10", "15"}, 0));
                list.add(new Question("15% of 200?", new String[]{"25", "30", "35"}, 1));
            }
        } else if (category.equals("Chemistry")) {
            if (difficulty.equals("Easy")) {
                list.add(new Question("Symbol for Oxygen?", new String[]{"Ox", "O", "O2"}, 1));
                list.add(new Question("Formula for water?", new String[]{"H2O", "HO2", "H2O2"}, 0));
            } else if (difficulty.equals("Medium")) {
                list.add(new Question("Atomic number of Carbon?", new String[]{"5", "6", "7"}, 1));
                list.add(new Question("What is NaCl?", new String[]{"Sugar", "Salt", "Acid"}, 1));
            } else {
                list.add(new Question("Laughing Gas?", new String[]{"N2O", "NO2", "CO2"}, 0));
                list.add(new Question("Most abundant gas in air?", new String[]{"Oxygen", "Nitrogen", "Argon"}, 1));
            }
        } else if (category.equals("History")) {
            if (difficulty.equals("Easy")) {
                list.add(new Question("First US President?", new String[]{"Lincoln", "Washington", "Jefferson"}, 1));
                list.add(new Question("Country with Pyramids?", new String[]{"Mexico", "Egypt", "China"}, 1));
            } else if (difficulty.equals("Medium")) {
                list.add(new Question("Year WWII ended?", new String[]{"1944", "1945", "1946"}, 1));
                list.add(new Question("Who painted Mona Lisa?", new String[]{"Da Vinci", "Picasso", "Dalí"}, 0));
            } else {
                list.add(new Question("French Revolution year?", new String[]{"1776", "1789", "1804"}, 1));
                list.add(new Question("Sun King of France?", new String[]{"Louis XIV", "Louis XVI", "Napoleon"}, 0));
            }
        } else if (category.equals("Sport")) {
            if (difficulty.equals("Easy")) {
                list.add(new Question("Soccer team size?", new String[]{"10", "11", "12"}, 1));
                list.add(new Question("Sport with a racket?", new String[]{"Tennis", "Football", "Golf"}, 0));
            } else if (difficulty.equals("Medium")) {
                list.add(new Question("World Cup 2022 winner?", new String[]{"France", "Brazil", "Argentina"}, 2));
                list.add(new Question("Marathon length (km)?", new String[]{"42.19", "35.5", "50.0"}, 0));
            } else {
                list.add(new Question("100m World Record?", new String[]{"Carl Lewis", "Usain Bolt", "Tyson Gay"}, 1));
                list.add(new Question("1st Modern Olympics city?", new String[]{"Paris", "London", "Athens"}, 2));
            }
        }
        return list;
    }
}