package pl.antc;

import pl.antc.algorithm.AlgorithmRunner;
import pl.antc.csv.PrepareData;

public class Main {

    public static void main(String[] args) throws Exception {
        //PrepareData.prepareAdultData();
        AlgorithmRunner.runCN2("data/adult/training.data", "data/adult/test.data", 2, 0.10);
        //PrepareData.prepareCarData();
    }
}
