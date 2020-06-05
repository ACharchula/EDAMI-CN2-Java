package pl.antc.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrepareData {

    public static void prepareAdultData() throws IOException {
        List<List<String>> data = CsvDataHandler.readCsv("data/adult/adult.data");
        data.removeIf(a -> a.contains("?"));

        discretization(data, 0);
        discretization(data, 2);
        discretization(data, 4);
        discretization(data, 10);
        discretization(data, 11);
        discretization(data, 12);

        List<List<String>> training = new ArrayList<>();
        List<List<String>> test = new ArrayList<>();
        divideData(data, training, test);

        CsvDataHandler.saveCsv(training, "data/adult/training.data");
        CsvDataHandler.saveCsv(test, "data/adult/test.data");
    }

    public static void prepareCarData() throws IOException {
        List<List<String>> data = CsvDataHandler.readCsv("data/cars/car.data");
        List<List<String>> training = new ArrayList<>();
        List<List<String>> test = new ArrayList<>();
        divideData(data, training, test);

        CsvDataHandler.saveCsv(training, "data/cars/training.data");
        CsvDataHandler.saveCsv(test, "data/cars/test.data");
    }

    private static void divideData(List<List<String>> data, List<List<String>> trainData, List<List<String>> testData) {
        int amountOfRecords = data.size() - 1; //minus attributes
        int threshold = (int) Math.floor(amountOfRecords * 0.8);
        List<String> attributes = data.get(0);

        trainData.add(attributes);
        trainData.addAll(data.subList(1, threshold+1));

        testData.add(attributes);
        testData.addAll(data.subList(threshold + 1, data.size()));
    }

    private static void discretization(List<List<String>> data, int column) {
        Set<String> possibleValues = new HashSet<>();
        for (List<String> row : data.subList(1, data.size())) {
            possibleValues.add(row.get(column));
        }

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (String value : possibleValues) {
            float v = Float.parseFloat(value);
            if (v < min) { min = v; }
            if (v > max) { max = v; }
        }

        float diff = (max - min) / 4;
        float firstIntervalEdge = min + diff;
        float secondIntervalEdge = firstIntervalEdge + diff;
        float thirdIntervalEdge = secondIntervalEdge + diff;
        String first = "[" + min + ";" + firstIntervalEdge + ")";
        String sec = "[" + firstIntervalEdge + ";" + secondIntervalEdge + ")";
        String third = "[" + secondIntervalEdge + ";" + thirdIntervalEdge + ")";
        String forth = "[" + thirdIntervalEdge + ";" + max + "]";

        for (List<String> row : data.subList(1, data.size())) {
            float value = Float.parseFloat(row.get(column));
            if (value >= min && value < firstIntervalEdge) {
                row.set(column, first);
            } else if (value >= firstIntervalEdge && value < secondIntervalEdge) {
                row.set(column, sec);
            } else if (value >= secondIntervalEdge && value < thirdIntervalEdge) {
                row.set(column, third);
            } else {
                row.set(column, forth);
            }
        }
    }
}
