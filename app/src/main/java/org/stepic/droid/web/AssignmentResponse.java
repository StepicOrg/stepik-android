package org.stepic.droid.web;

import org.stepic.droid.model.Meta;
import org.stepik.android.model.learning.Assignment;

import java.util.List;

public class AssignmentResponse extends MetaResponseBase {
    private final List<Assignment> assignments;

    public AssignmentResponse(Meta meta, List<Assignment> assignmentList) {
        super(meta);
        this.assignments = assignmentList;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }
}
