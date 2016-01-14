package org.stepic.droid.web;

import org.stepic.droid.model.Submission;

import java.util.List;

public class SubmissionResponse {
    public List<Submission> getSubmissions() {
        return submissions;
    }

    List<Submission> submissions;
}
