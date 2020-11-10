package readability;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final HashMap<Integer, String> ageRanges = new HashMap<>();

    static {
        ageRanges.put(1, "5-6");
        ageRanges.put(2, "6-7");
        ageRanges.put(3, "7-9");
        ageRanges.put(4, "9-10");
        ageRanges.put(5, "10-11");
        ageRanges.put(6, "11-12");
        ageRanges.put(7, "12-13");
        ageRanges.put(8, "13-14");
        ageRanges.put(9, "14-15");
        ageRanges.put(10, "15-16");
        ageRanges.put(11, "16-17");
        ageRanges.put(12, "17-18");
        ageRanges.put(13, "18-24");
        ageRanges.put(14, "24+");
    }


    public static void main(String[] args) throws IOException {

        if (args.length > 0) {
            String contents = new String(Files.readAllBytes(Paths.get(args[0])));
            Scanner sc = new Scanner(System.in);
            getTextStats(contents, true);

            System.out.println("Enter the score you want to calculate (ARI, FK, SMOG, CL, all)");
            String input = sc.nextLine();

            double ariScore =  findAriScore(contents);
            double fkScore =  findFKReadability(contents);
            double smog = findSmogIndex(contents);
            double cli = findCliIndex(contents);
            int ariAge = getAgeFromTable((int) Math.floor(ariScore));
            int fkAge = getAgeFromTable((int) Math.floor(fkScore));
            int smogAge = getAgeFromTable((int) Math.floor(smog));
            int cliAge = getAgeFromTable((int) Math.ceil(cli));


            switch (input) {
                case "ARI":

                    System.out.println(String.format("Automated Readability Index: %.2f (about " + ariAge + " year olds).", ariScore));
                    break;
                case "FK":

                    System.out.println(String.format("Flesch–Kincaid readability tests: %.2f (about " + fkAge + " year olds).", fkScore));
                    break;
                case "SMOG":

                    System.out.println(String.format("Simple Measure of Gobbledygook: %.2f (about " + smogAge + " year olds).", smog));
                    break;
                case "CL":

                    System.out.println(String.format("Coleman–Liau index: %.2f (about " + cliAge + " year olds)", cli));
                    break;
                case "all":
                    double average = (ariAge + smogAge + cliAge + fkAge) / 4.0;
                    System.out.println(String.format("Automated Readability Index: %.2f (about " + ariAge + " year olds).", ariScore));
                    System.out.println(String.format("Flesch–Kincaid readability tests: %.2f (about " + fkAge + " year olds).", fkScore));
                    System.out.println(String.format("Simple Measure of Gobbledygook: %.2f (about " + smogAge + " year olds).", smog));
                    System.out.println(String.format("Coleman–Liau index: %.2f (about " + cliAge + " year olds)\n", cli));
                    System.out.println(String.format("This text should be understood in average by %.2f year olds.", average));
            }



        }
    }

    public static int getAgeFromTable(int score) {
        if (ageRanges.containsKey(score)) {
            if (ageRanges.get(score).contains("-")) {
                return Integer.parseInt(ageRanges.get(score).split("-")[1]);
            } else {
                return 24;
            }
        }
        return 0;
    }

    public static int characterCount(String text) {
        String characters = text.replaceAll("[ \t\n]", "");
        return characters.length();
    }

    public static int[] getTextStats(String text, boolean printStats) {

        int polysyllableCount = 0;

        int syllableCount = 0;

        String[] sentences = text.split("[.!?]");
        int sentenceCount = sentences.length;
        int totalWordsCount = 0;
        for (String s : sentences) {
            String[] temp = s.trim().split(" ");
            totalWordsCount += temp.length;
            for (String str : temp) {
                String regex = "[aeiouyAEIOUY]+";
                String formattedStr = str.replaceAll("\\W", "").replaceAll("e\\b", "");


                Pattern pattern = Pattern.compile(regex);
                Matcher m = pattern.matcher(formattedStr);
                int localWordSyllableCount = 0;
                while (m.find()) {
                    localWordSyllableCount++;
                }
                if (localWordSyllableCount == 0) {
                    syllableCount++;
                }
                syllableCount += localWordSyllableCount;
                if (localWordSyllableCount > 2) {
                    polysyllableCount++;
                }
            }
        }

        if (printStats == true) {
            System.out.println("Words: " + totalWordsCount);
            System.out.println("Sentences: " + sentenceCount);
            System.out.println("Characters: " + characterCount(text));
            System.out.println("Syllables: " + syllableCount);
            System.out.println("Polysyllables: " + polysyllableCount);
        }
        return new int[]{sentenceCount, totalWordsCount, syllableCount, polysyllableCount};
    }

    public static void checkDifficulty(String text) {
        int[] stats = getTextStats(text, false);
        int sentenceCount = stats[0];
        int wordCount = stats[1];
        int syllableCount = stats[2];
        int polysyllableCount = stats[3];
        int characterCount = characterCount(text);


        double ariScore = (4.71 * (1.0 * characterCount / wordCount)) + 0.5 * (1.0 * wordCount / sentenceCount) - 21.43;
        double twoDecimalScore = Math.round(ariScore * 100.0) / 100.0;
        int roundedScore = (int) Math.ceil(ariScore);
        System.out.println("Words: " + wordCount);
        System.out.println("Sentences: " + sentenceCount);
        System.out.println("Characters: " + characterCount);
        System.out.println(String.format("The score is: %.2f", twoDecimalScore));
        System.out.println("This text should be understood by " + ageRanges.get(roundedScore) + " year olds.");
        System.out.println("Syllables: " + syllableCount);
        System.out.println("Polysyllables: " + polysyllableCount);





    }

    public static double findSmogIndex(String text) {
        int[] stats = getTextStats(text, false);
        int sentenceCount = stats[0];
        int polysyllableCount = stats[3];
        return 1.043 * Math.sqrt(polysyllableCount * 1.0 * 30 / sentenceCount) + 3.1291;
    }

    public static double findCliIndex(String text) {
        int[] stats = getTextStats(text, false);
        int characterCount = characterCount(text);
        int sentenceCount = stats[0];
        int wordCount = stats[1];
        double L = 1.0 * characterCount / wordCount * 100;
        double S = 1.0 * sentenceCount / wordCount * 100;
        return 0.0588 * L - 0.296 * S - 15.8;
    }

    public static double findAriScore(String text) {
        int[] stats = getTextStats(text, false);
        int sentenceCount = stats[0];
        int wordCount = stats[1];
        int characterCount = characterCount(text);
        double ariScore = (4.71 * (1.0 * characterCount / wordCount)) + 0.5 * (1.0 * wordCount / sentenceCount) - 21.43;
        return ariScore;
    }

    public static double findFKReadability(String text) {
        int[] stats = getTextStats(text, false);
        int characterCount = characterCount(text);
        int sentenceCount = stats[0];
        int wordCount = stats[1];
        int syllableCount = stats[2];
        return 0.39 * (1.0*wordCount/sentenceCount) + 11.8 * (1.0*syllableCount/wordCount) - 15.59;
    }

}


