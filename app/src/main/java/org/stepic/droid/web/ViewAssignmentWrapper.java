package org.stepic.droid.web;

public class ViewAssignmentWrapper {
    ViewAssignment view;

    public ViewAssignmentWrapper(long assignmentId, long stepId) {
        view = new ViewAssignment(assignmentId, stepId);
    }

    public long getAssignment() {
        return view.getAssignment();
    }

    public long getStep() {
        return view.getStep();
    }
}
