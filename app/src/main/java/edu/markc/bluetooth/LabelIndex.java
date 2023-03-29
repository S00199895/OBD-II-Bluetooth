package edu.markc.bluetooth;

public class LabelIndex {
    public float index;
    public String Label;


    public LabelIndex(float i, String l)
    {
        this.index =i;
        this.Label=l;


    }

    @Override
    public String toString() {
        return "LabelIndex{" +
                "index=" + index +
                ", Label='" + Label + '\'' +
                '}';
    }
}
