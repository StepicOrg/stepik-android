package org.stepic.droid.web;

import com.google.gson.annotations.SerializedName;

public class ViewAssignment {
    long assignment;
    long step;

    public ViewAssignment(long assignment, long stepId) {
        this.assignment = assignment;
        this.step = stepId;
    }

    public long getAssignment() {
        return assignment;
    }

    public long getStep() {
        return step;
    }
}
