package pl.antc;

import pl.antc.algorithm.CN2;
import pl.antc.model.Rule;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        //PrepareData.prepareAdultData();
        CN2 cn2 = new CN2();
        List<Rule> rules =  cn2.train("data/adult/training.data", 7, 0.5);
        cn2.test("data/adult/test.data", rules);
    }
}
