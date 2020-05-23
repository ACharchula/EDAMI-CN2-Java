package pl.antc.model;

import java.util.List;

public class Rule {
    List<Selector> complex;
    String result;

    public Rule(List<Selector> complex, String result) {
        this.complex = complex;
        this.result = result;
    }

    public List<Selector> getComplex() {
        return complex;
    }

    public void setComplex(List<Selector> complex) {
        this.complex = complex;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
