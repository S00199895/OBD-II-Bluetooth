package edu.markc.bluetooth;

public enum Importance {
HIGH("High"),
MEDIUM("Medium"),
LOW("Low");

private String name;

private Importance(String name)
{
    this.name = name;
}

    @Override
    public String toString() {
        return name;
    }
}
