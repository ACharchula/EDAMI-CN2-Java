package pl.antc.model;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public String toString() {
        List<String> cpx = complex.stream().map(Selector::toString).collect(Collectors.toList());
        return String.join(",", cpx) + " => " + result;
    }
}
