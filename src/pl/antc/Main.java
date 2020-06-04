package pl.antc;

import pl.antc.algorithm.CN2;

public class Main {

    public static void main(String[] args) throws Exception {
        //PrepareData.prepareAdultData();
        CN2 cn2 = new CN2();
        cn2.trainAndTest("data/adult/training.data", "data/adult/test.data", 4, 0.5);
    }
}
