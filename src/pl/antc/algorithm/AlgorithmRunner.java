package pl.antc.algorithm;

import pl.antc.model.Rule;
import pl.antc.model.TestResults;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AlgorithmRunner {

    public static void runCN2(String trainFilePath, String testFilePath, int starMaxSize, double minSignificance) throws IOException {
        CN2 cn2 = new CN2();

        System.out.println("=== TRAINING ===");
        System.out.println("Max size of star: " + starMaxSize);
        System.out.println("Min significance: " + minSignificance);

        long startTime = new Date().getTime();
        List<Rule> ruleList = cn2.train(trainFilePath, starMaxSize, minSignificance);
        long endTime = new Date().getTime();

        System.out.println("Amount of rules: " + ruleList.size());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(endTime-startTime);
        long minutesInSeconds = TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(endTime-startTime-minutesInSeconds);
        System.out.println("Training time: " + minutes + " min " + seconds + " s");

        System.out.println("=== TESTING === ");

        TestResults results = cn2.test(testFilePath, ruleList);

        System.out.println("Amount of test rows: " + results.getAmountOfTestRows());
        System.out.println("Correct: " + results.getCorrect());
        System.out.println("Incorrect: " + results.getIncorrect());
        System.out.println("Rule not found: " + results.getNotCoveredByAnyRule());
        System.out.println("Accuracy: " + (float) results.getCorrect() / results.getAmountOfTestRows());
    }
}
