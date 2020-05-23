package pl.antc.csv;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvDataHandler {

    public static List<List<String>> readCsv(String filePath) throws IOException {
        List<List<String>> records = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }
        }
        return records;
    }

    public static void saveCsv(List<List<String>> data, String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        try (PrintWriter pw = new PrintWriter(file)) {
            data.stream()
                    .map(CsvDataHandler::convertToCsvLine)
                    .forEach(pw::println);
        }
    }

    private static String convertToCsvLine(List<String> values) {
        return String.join(",", values);
    }
}
