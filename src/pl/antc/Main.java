package pl.antc;

import pl.antc.algorithm.CN2;
import pl.antc.csv.PrepareData;

public class Main {

    public static void main(String[] args) throws Exception {
        PrepareData.prepareAdultData();
        CN2 cn2 = new CN2();
        cn2.train("data/adult/training.data");
    }
}
