package pl.antc.algorithm;

import pl.antc.csv.CsvDataHandler;
import pl.antc.model.Rule;
import pl.antc.model.Selector;
import pl.antc.model.TextValue;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CN2 {

    private List<List<String>> data;
    private List<List<String>> E = new ArrayList<>();
    private final List<Selector> selectors = new ArrayList<>();
    private final List<Selector> possibleResults = new ArrayList<>();
    private Map<String, Integer> attributes = new HashMap<>();
    private Map<String, Double> allResultsProbability = new HashMap<>();

    private int starMaxSize = 5;
    private double minSignificance = 0.8;

    List<Rule> ruleList;

    public void train(String filePath) throws IOException {
        data = CsvDataHandler.readCsv(filePath);
        E = CsvDataHandler.copy(data).subList(1, data.size());
        prepare(data);

        List<Rule> ruleList = new ArrayList<>();

        while (E.size() > 0) {
            List<Selector> bestCpx = findBestComplex();
            if (bestCpx != null) {
                List<Integer> covered = coveredRowsIndices(E, bestCpx);
                TextValue mostCommonClass = getMostCommon(covered); //TODO get rid of text value as we not use value
                List<List<String>> newE = new ArrayList<>();
                for (int i = 0; i < E.size(); ++i) {
                    if (!covered.contains(i)){
                        newE.add(E.get(i));
                    }
                }
                E = newE;

                ruleList.add(new Rule(bestCpx, mostCommonClass.getText()));
                List<String> cpx = bestCpx.stream().map(Selector::toString).collect(Collectors.toList());
                System.out.println(String.join(",", cpx) + " => " + mostCommonClass.getText() + "(" + mostCommonClass.getValue() + ")");
            } else {
                break;
            }
        }
    }

    public void test(String filePath) throws IOException {
        List<List<String>> testData = CsvDataHandler.readCsv(filePath);
        testData = testData.subList(1, testData.size());

        for (List<String> row : testData) {

        }

    }

    private void prepare(List<List<String>> data) {
        List<String> attributes = data.get(0);

        for (int i = 0; i < attributes.size(); ++ i) {
            this.attributes.put(attributes.get(i), i);
        }

        List<String> results = data.subList(1, data.size()).stream().map(a -> a.get(a.size()-1)).collect(Collectors.toList());
        allResultsProbability = getResultProbability(results);

        for (int i = 0; i < attributes.size(); i++) {
            Set<String> possibleValues = new HashSet<>();
            for (List<String> row : data.subList(1, data.size())) {
                possibleValues.add(row.get(i));
            }

            if (i == attributes.size() - 1) {
                for (String value : possibleValues) {
                    possibleResults.add(new Selector(attributes.get(i), value));
                }
            } else {
                for (String value : possibleValues) {
                    selectors.add(new Selector(attributes.get(i), value));
                }
            }
        }
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
                if (coveredRowsResults.size() == 1) continue; //TODO check if significance is well
                HashMap<String, Double> resultProb = getResultProbability(coveredRowsResults);
                double significance = calculateSignificance(resultProb);
                if (significance > minSignificance) {
                    double entropy = calculateEntropy(resultProb);
                    entropyMeasures.put(entropy, i);
                    if (entropy < bestComplexEntropy) {
                        bestComplex = cloneComplex(complex);
                        bestComplexEntropy = entropy;
                        bestComplexSignificance = significance;
                        if (entropy == 0) {
                            break;
                        }
                    }
                }
            }
            if (bestComplexEntropy == 0)
                break;
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

    private TextValue getMostCommon(List<Integer> indices) {
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
        TextValue max = null;
        int maxV = -1;

        for (String key : amount.keySet()) {
            if (amount.get(key) > maxV) {
                max = new TextValue(key, (((float) amount.get(key))/ indices.size()));
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
