package pl.antc.model;

public class Selector {
    String attribute;
    String value;

    public Selector(String attribute, String value) {
        this.attribute = attribute;
        this.value = value;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Selector clone() {
        return new Selector(this.attribute, this.value);
    }

    public boolean equalAttributes(Selector selector) {
        return this.attribute.equals(selector.attribute);
    }

    @Override
    public String toString() {
        return "("+attribute+"->"+value+")";
    }
}
