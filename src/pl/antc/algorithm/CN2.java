package pl.antc.algorithm;

import pl.antc.csv.CsvDataHandler;
import pl.antc.model.Rule;
import pl.antc.model.Selector;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CN2 {

    private List<List<String>> data;
    private List<List<String>> E = new ArrayList<>();
    private final List<Selector> selectors = new ArrayList<>();
    private final List<Selector> possibleResults = new ArrayList<>();
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
                List<Integer> covered = coveredRowsIndices(E, bestComplex);
                String mostCommonClass = getMostCommon(covered);
                List<List<String>> newE = new ArrayList<>();
                for (int i = 0; i < E.size(); ++i) {
                    if (!covered.contains(i)){
                        newE.add(E.get(i));
                    }
                }
                E = newE;

                countProbabilityForResultsInE();

                ruleList.add(new Rule(bestComplex, mostCommonClass));
                List<String> cpx = bestComplex.stream().map(Selector::toString).collect(Collectors.toList());
                System.out.println(String.join(",", cpx) + " => " + mostCommonClass + "(" + E.size() + ")");
            } else {
                break;
            }
        }
        return ruleList;
    }

    private void prepare(String filePath) throws IOException {
        data = CsvDataHandler.readCsv(filePath);
        E = data.subList(1, data.size());
        List<String> attributes = data.get(0);

        for (int i = 0; i < attributes.size(); ++ i) {
            this.attributes.put(attributes.get(i), i);
        }

        countProbabilityForResultsInE();

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
//            if (i == attributes.size() - 1) {
//                for (String value : possibleValues) {
//                    possibleResults.add(new Selector(attributes.get(i), value));
//                }
//            } else {
//                for (String value : possibleValues) {
//                    selectors.add(new Selector(attributes.get(i), value));
//                }
//            }
        }
    }

    private void countProbabilityForResultsInE() {
        List<String> results = E.stream().map(a -> a.get(a.size()-1)).collect(Collectors.toList());
        allResultsProbability = getResultProbability(results);
    }

    private List<Selector> findBestComplex() {
        List<Selector> bestComplex = null;
        double bestComplexEntropy = Double.MAX_VALUE;
        double bestComplexSignificance = 0;
        List<List<Selector>> star = new ArrayList<>();
        do {
            Map<Double, Integer> entropyMeasures = new HashMap<>();
            List<List<Selector>> newStar = setNewStar(star);
            for (int i = 0; i < newStar.size(); ++i) {
                List<Selector> complex = newStar.get(i);
                List<String> coveredRowsResults = getResultsOfCoveredRows(E, complex);
                HashMap<String, Double> resultProb = getResultProbability(coveredRowsResults); //TODO maybe all result prop should change with E
                double significance = calculateSignificance(resultProb);
                if (significance > minSignificance) {
                    double entropy = calculateEntropy(resultProb);
                    if (entropy == 0)
                        return cloneComplex(complex);
                    entropyMeasures.put(entropy, i);
                    if (entropy < bestComplexEntropy) {
                        bestComplex = cloneComplex(complex);
                        bestComplexEntropy = entropy;
                        bestComplexSignificance = significance;
                    }
                }
            }
            List<Double> entropies = new ArrayList<>(entropyMeasures.keySet());
            if (entropies.size() == 0) {
                break;
            }
            Collections.sort(entropies);
            List<Double> bestEntropies = null;
            if (entropies.size() < starMaxSize) {
                bestEntropies = entropies;
            } else {
                bestEntropies = entropies.subList(0, starMaxSize);
            }
            star.clear();
            for (Double entropy : bestEntropies) {
                star.add(newStar.get(entropyMeasures.get(entropy)));
            }
        } while (star.size() != 0 && !(bestComplexSignificance < minSignificance));

        return bestComplex;
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

        System.out.println("Correct: " + correct);
        System.out.println("Incorrect: " + incorrect);
        System.out.println("Rule not found: " + ruleNotFound);
        System.out.println("Accuracy: " + (float) correct / (incorrect+ruleNotFound));
    }

    private int predict(List<String> data, List<Rule> rules, List<String> columns) {
        for (Rule rule : rules) {
            if (isComplexMatching(data, rule, columns)) {
                if (rule.getResult().equals(data.get(data.size() - 1))) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
        return 0;
    }

    private boolean isComplexMatching(List<String> data, Rule rule, List<String> columns) {
        for (Selector selector : rule.getComplex()) {
            for (int i = 0; i < data.size(); ++i) {
                if (columns.get(i).equals(selector.getAttribute())) {
                    if (!data.get(i).equals(selector.getValue()))
                        return false;
                }
            }
        }
        return true;
    }

    private String getMostCommon(List<Integer> indices) {
        List<String> results = new ArrayList<>();
        for (Integer i : indices) {
            results.add(E.get(i).get(E.get(0).size() - 1));
        }
        Map<String, Integer> amount = new HashMap<>();
        for (String key : results) {
            Integer a = amount.get(key);
            if (a == null) {
                amount.put(key, 1);
            } else {
                amount.replace(key, a+1);
            }
        }
        String max = null;
        int maxV = -1;

        for (String key : amount.keySet()) {
            if (amount.get(key) > maxV) {
                max = key;
            }
        }
        return max;
    }

    private List<Selector> cloneComplex(List<Selector> complex) { //TODO duplicate
        List<Selector> cloned = new ArrayList<>();
        for (Selector selector : complex) {
            cloned.add(selector.clone());
        }
        return cloned;
    }

    private double calculateSignificance(HashMap<String, Double> resultProb) {
        List<Double> tmp = new ArrayList<>();
        for (String key : resultProb.keySet()) {
            double log = Math.log(resultProb.get(key) / allResultsProbability.get(key));
            tmp.add(resultProb.get(key) * log);
        }
        return tmp.stream().mapToDouble(Double::doubleValue).sum() * 2;
    }

    private double calculateEntropy(HashMap<String, Double> resultProb) {
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

//        for (String key : allResultsProbability.keySet()) {
//            map.putIfAbsent(key, 0.0);
//        }

        return map;
    }

    private List<String> getResultsOfCoveredRows(List<List<String>> data, List<Selector> complex) {
        List<Integer> coveredRows = coveredRowsIndices(data, complex);
        List<String> result = new ArrayList<>();
        for (Integer i : coveredRows) {
            result.add(data.get(i).get(data.get(0).size() - 1));
        }
        return result;
    }

    private List<Integer> coveredRowsIndices(List<List<String>> data, List<Selector> complex) {
        List<Integer> coveredRows = IntStream.range(0, data.size()).boxed().collect(Collectors.toList()); //TODO check range
        for (Selector selector : complex) {
            coveredRows.removeIf(i -> !isRowCovered(data.get(i), selector));
        }
        return coveredRows;
    }

    private boolean isRowCovered(List<String> row, Selector selector) {
        int index = this.attributes.get(selector.getAttribute());
        String value = row.get(index);
        return value.equals(selector.getValue());
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
            if (s.attEq(selector)) {
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
