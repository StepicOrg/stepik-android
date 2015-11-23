package org.stepic.droid.web;

public class ViewAssignmentWrapper {
    long assignment;
    long step;

    public ViewAssignmentWrapper(long assignment, long stepId) {
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
