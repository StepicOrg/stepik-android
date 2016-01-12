package org.stepic.droid.web;

import org.stepic.droid.model.Attempt;

public class AttemptRequest {
    Attempt attempt;

    public AttemptRequest(long stepId) {
        attempt = new Attempt(stepId);
    }
}
