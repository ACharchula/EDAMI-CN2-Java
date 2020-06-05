package pl.antc.model;

public class TestResults {
    private int correct = 0;
    private int incorrect = 0;
    private int notCoveredByAnyRule = 0;
    private int amountOfTestRows = 0;

    public void incrementCorrect() {
        correct++;
    }

    public void incrementIncorrect() {
        incorrect++;
    }

    public void incrementNotCovered() {
        notCoveredByAnyRule++;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }

    public int getIncorrect() {
        return incorrect;
    }

    public void setIncorrect(int incorrect) {
        this.incorrect = incorrect;
    }

    public int getNotCoveredByAnyRule() {
        return notCoveredByAnyRule;
    }

    public void setNotCoveredByAnyRule(int notCoveredByAnyRule) {
        this.notCoveredByAnyRule = notCoveredByAnyRule;
    }

    public int getAmountOfTestRows() {
        return amountOfTestRows;
    }

    public void setAmountOfTestRows(int amountOfTestRows) {
        this.amountOfTestRows = amountOfTestRows;
    }
}
