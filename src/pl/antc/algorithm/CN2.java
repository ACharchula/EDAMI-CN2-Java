package pl.antc.algorithm;

import pl.antc.csv.CsvDataHandler;
import pl.antc.model.Selector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CN2 {

    private List<String> E = new ArrayList<>();
    private List<Selector> selectors = new ArrayList<>();
    private List<Selector> possibleResults = new ArrayList<>();
    private int starMaxSize = 5;
    private double minSignificance = 0.5;

    public String train(String filePath) throws IOException {
        List<List<String>> data = CsvDataHandler.readCsv(filePath);
        prepareSelectors(data);

        return null;
    }

    private void prepareSelectors(List<List<String>> data) {
        List<String> attributes = data.get(0);
        for (int i = 0; i < attributes.size(); i++) {
            Set<String> possibleValues = new HashSet<>();
            for (List<String> row : data.subList(1, data.size()-1)) {
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
}
