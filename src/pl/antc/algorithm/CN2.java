package pl.antc.algorithm;

import pl.antc.csv.CsvDataHandler;
import pl.antc.model.Rule;
import pl.antc.model.Selector;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CN2 {

    private List<List<String>> E = new ArrayList<>();
    private final List<Selector> selectors = new ArrayList<>();
    private final Map<String, Integer> attributes = new HashMap<>();
    private Map<String, Double> allResultsProbability = new HashMap<>();

    private int starMaxSize;
    private double minSignificance;

    public List<Rule> train(String filePath, int starMaxSize, double minSignificance) throws IOException {
        this.starMaxSize = starMaxSize;
        this.minSignificance = minSignificance;
        prepare(filePath);

        List<Rule> ruleList = new ArrayList<>();

        while (E.size() > 0) {
            List<Selector> bestComplex = findBestComplex();
            if (bestComplex != null) {
                String mostCommonResult = processComplex(bestComplex);
                ruleList.add(new Rule(bestComplex, mostCommonResult));
//                Rule rule = new Rule(bestComplex, mostCommonResult);
//                ruleList.add(rule);
//                System.out.println(rule + " (" + E.size() + ")");
            } else {
                break;
            }
        }
        return ruleList;
    }

    public void trainAndTest(String trainFilePath, String testFilePath, int starMaxSize, double minSignificance) throws IOException {
        System.out.println("=== TRAINING ===");
        System.out.println("Max size of star: " + starMaxSize);
        System.out.println("Min significance: " + minSignificance);
        long startTime = new Date().getTime();
        List<Rule> ruleList = train(trainFilePath, starMaxSize, minSignificance);
        long endTime = new Date().getTime();
        System.out.println("Amount of rules: " + ruleList.size());
        System.out.println("Training time [minutes]: " + TimeUnit.MILLISECONDS.toMinutes(endTime - startTime));


        test(testFilePath, ruleList);
    }

    private String processComplex(List<Selector> bestComplex) {
        Map<String, Integer> coveredRowsResults = new HashMap<>();
        Map<String, Integer> leftRowsResults = new HashMap<>();

        E.removeIf(row -> {
            String result = row.get(row.size()-1);
            if (isComplexCoveringRow(row, bestComplex)) {
                addNewOrIncrement(coveredRowsResults, result);
                return true;
            } else {
                addNewOrIncrement(leftRowsResults, result);
                return false;
            }
        });

        for (String key : leftRowsResults.keySet()) {
            double probability = (float) leftRowsResults.get(key) / E.size();
            allResultsProbability.replace(key, probability);
        }

        String max = null;
        int maxV = -1;

        for (String key : coveredRowsResults.keySet()) {
            int result = coveredRowsResults.get(key);
            if (result > maxV) {
                maxV = result;
                max = key;
            }
        }
        return max;
    }

    private Map<String, Double> getCoveredResultsProbabilities(List<Selector> complex) {
        Map<String, Integer> coveredRowsResults = new HashMap<>();
        int amountOfCoveredRows = 0;
        for (List<String> row : E) {
            if (isComplexCoveringRow(row, complex)) {
                addNewOrIncrement(coveredRowsResults, row.get(row.size()-1));
                amountOfCoveredRows++;
            }
        }
        Map<String, Double> probabilities = new HashMap<>();
        for (String key : coveredRowsResults.keySet()) {
            double probability = (float) coveredRowsResults.get(key) / amountOfCoveredRows;
            probabilities.put(key, probability);
        }
        return probabilities;
    }

    private void addNewOrIncrement(Map<String, Integer> map, String key) {
        Integer value = map.get(key);
        if (value != null) {
            map.replace(key, ++value);
        } else {
            map.put(key, 1);
        }
    }

    private boolean isComplexCoveringRow(List<String> row, List<Selector> complex) {
        for (Selector selector : complex) {
            int index = attributes.get(selector.getAttribute());
            if (!row.get(index).equals(selector.getValue()))
                return false;
        }
        return true;
    }

    private void prepare(String filePath) throws IOException {
        List<List<String>> data = CsvDataHandler.readCsv(filePath);
        E = data.subList(1, data.size());
        List<String> attributes = data.get(0);

        for (int i = 0; i < attributes.size(); ++ i) {
            this.attributes.put(attributes.get(i), i);
        }

        List<String> results = E.stream().map(a -> a.get(a.size()-1)).collect(Collectors.toList());
        allResultsProbability = getResultProbability(results);

        for (int i = 0; i < attributes.size(); i++) {
            Set<String> possibleValues = new HashSet<>();
            for (List<String> row : data.subList(1, data.size())) {
                possibleValues.add(row.get(i));
            }

            if (i != attributes.size()-1) {
                for (String value : possibleValues) {
                    selectors.add(new Selector(attributes.get(i), value));
                }
            }
        }
    }

    private List<Selector> findBestComplex() {
        List<Selector> bestComplex = null;
        double bestComplexEntropy = Double.MAX_VALUE;
        double bestComplexSignificance = 0.0;
        List<List<Selector>> star = new ArrayList<>();
        do {
            Map<Double, Integer> entropyMeasures = new HashMap<>();
            List<List<Selector>> newStar = setNewStar(star);
            for (int i = 0; i < newStar.size(); ++i) {
                List<Selector> complex = newStar.get(i);
                Map<String, Double> resultProb = getCoveredResultsProbabilities(complex);
                double significance = calculateSignificance(resultProb);
                if (significance > minSignificance) {
                    double entropy = calculateEntropy(resultProb);
                    if (entropy == 0.0) return cloneComplex(complex);
                    entropyMeasures.put(entropy, i);
                    if (entropy < bestComplexEntropy) {
                        bestComplex = cloneComplex(complex);
                        bestComplexEntropy = entropy;
                        bestComplexSignificance = significance;
                    }
                }
            }

            if (entropyMeasures.keySet().size() == 0) break;
            star = getNewStar(entropyMeasures, newStar);
        } while (star.size() != 0 && !(bestComplexSignificance < minSignificance));

        return bestComplex;
    }

    private List<List<Selector>> getNewStar(Map<Double, Integer> entropyMeasures, List<List<Selector>> previousStar) {
        List<Double> entropies = new ArrayList<>(entropyMeasures.keySet());
        Collections.sort(entropies);
        List<Double> bestEntropies;
        if (entropies.size() <= starMaxSize) {
            bestEntropies = entropies;
        } else {
            bestEntropies = entropies.subList(0, starMaxSize);
        }
        List<List<Selector>> newStar = new ArrayList<>();
        for (Double entropy : bestEntropies) {
            newStar.add(previousStar.get(entropyMeasures.get(entropy)));
        }
        return newStar;
    }

    public void test(String filePath, List<Rule> rules) throws IOException {
        List<List<String>> testData = CsvDataHandler.readCsv(filePath);
        List<String> columns = testData.get(0);
        testData = testData.subList(1, testData.size());

        int correct = 0;
        int incorrect = 0;
        int ruleNotFound = 0;
        for (List<String> row : testData) {
            int result = predict(row, rules, columns);
            if (result == 1) correct++;
            else if (result == -1) incorrect++;
            else ruleNotFound++;
        }

        System.out.println("=== TESTING === ");
        System.out.println("Amount of test rows: " + testData.size());
        System.out.println("Correct: " + correct);
        System.out.println("Incorrect: " + incorrect);
        System.out.println("Rule not found: " + ruleNotFound);
        System.out.println("Accuracy: " + (float) correct / (incorrect+ruleNotFound));
    }

    private int predict(List<String> data, List<Rule> rules, List<String> columns) {
        for (Rule rule : rules) {
            if (isComplexCoveringRow(data, rule.getComplex())) {
                if (rule.getResult().equals(data.get(data.size() - 1))) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
        return 0;
    }

    private List<Selector> cloneComplex(List<Selector> complex) { //TODO duplicate
        List<Selector> cloned = new ArrayList<>();
        for (Selector selector : complex) {
            cloned.add(selector.clone());
        }
        return cloned;
    }

    private double calculateSignificance(Map<String, Double> resultProb) {
        List<Double> tmp = new ArrayList<>();
        for (String key : resultProb.keySet()) {
            double log = Math.log(resultProb.get(key) / allResultsProbability.get(key));
            tmp.add(resultProb.get(key) * log);
        }
        return tmp.stream().mapToDouble(Double::doubleValue).sum() * 2;
    }

    private double calculateEntropy(Map<String, Double> resultProb) {
        List<Double> tmp = new ArrayList<>();
        for (String key : resultProb.keySet()) {
            double log = log2(resultProb.get(key));
            tmp.add(resultProb.get(key) * log);
        }
        return tmp.stream().mapToDouble(Double::doubleValue).sum() * (-1);
    }

    private double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    private HashMap<String, Double> getResultProbability(List<String> results) {
        HashMap<String, Double> map = new HashMap<>();
        for (String result : results) {
            Double a = map.get(result);
            if (a == null) {
                map.put(result, 1.0);
            } else {
                map.replace(result, a+1);
            }
        }
        for (String key : map.keySet()) {
            double probability = map.get(key) / results.size();
            map.replace(key, probability);
        }
        return map;
    }

    private List<List<Selector>> setNewStar(List<List<Selector>> star) {
        List<List<Selector>> newStar = new ArrayList<>();
        if (star.size() > 0) {
            for (List<Selector> complex : star) {
                for (Selector selector : selectors) {
                    List<Selector> newComplex = returnNewComplexIfNotADuplicate(complex, selector);
                    if (newComplex != null)
                        newStar.add(newComplex);
                }
            }
        } else {
            for (Selector selector : selectors) {
                List<Selector> complex = new ArrayList<>();
                complex.add(selector);
                newStar.add(complex);
            }
        }
        return newStar;
    }

    private List<Selector> returnNewComplexIfNotADuplicate(List<Selector> complex, Selector selector) {
        for (Selector s : complex) {
            if (s.equalAttributes(selector)) {
                return null;
            }
        }

        List<Selector> newList = new ArrayList<>();
        for (Selector s : complex) {
            newList.add(s.clone());
        }
        newList.add(selector);
        return newList;
    }
}
