package org.stepic.droid.events.units;

public class UnitScoreUpdateEvent {

    long unitId;
    double newScore;

    public UnitScoreUpdateEvent(long unitId, double score) {
        this.unitId = unitId;
        newScore = score;
    }

    public double getNewScore() {
        return newScore;
    }

    public long getUnitId() {
        return unitId;
    }
}
