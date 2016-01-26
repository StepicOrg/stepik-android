package org.stepic.droid.model;

public class Option {
    private String value;
    private int positionId;

    public Option(String value, int positionId) {
        this.value = value;
        this.positionId = positionId;
    }

    public String getValue() {
        return value;
    }

    public int getPositionId() {
        return positionId;
    }
}
