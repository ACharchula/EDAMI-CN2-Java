package pl.antc.csv;

import java.io.IOException;
import java.util.*;

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

        splitAndSave("data/adult/", data);
    }

    public static void prepareCarData() throws IOException {
        splitAndSave("data/cars/", "car.data");
    }

    public static void prepareNurseryData() throws IOException {
        splitAndSave("data/nursery/", "nursery.data");
    }

    private static void splitAndSave(String directory, List<List<String>> data) throws IOException {
        List<List<String>> training = new ArrayList<>();
        List<List<String>> test = new ArrayList<>();

        List<String> attributes = data.get(0);

        List<List<String>> rows = data.subList(1, data.size());
        Collections.shuffle(rows);

        rows.add(0, attributes);
        divideData(rows, training, test);

        CsvDataHandler.saveCsv(training, directory + "training.data");
        CsvDataHandler.saveCsv(test, directory + "test.data");
    }

    private static void splitAndSave(String directory, String sourceFileName) throws IOException {
        List<List<String>> data = CsvDataHandler.readCsv(directory + sourceFileName);
        splitAndSave(directory, data);
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
