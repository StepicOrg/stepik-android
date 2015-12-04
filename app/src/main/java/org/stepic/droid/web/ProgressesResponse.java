package org.stepic.droid.web;

import org.stepic.droid.model.Meta;
import org.stepic.droid.model.Progress;

import java.util.List;

public class ProgressesResponse extends StepicResponseBase {

    List<Progress> progresses;

    public ProgressesResponse(Meta meta, List<Progress> progresses) {
        super(meta);
        this.progresses = progresses;
    }

    public List<Progress> getProgresses() {
        return progresses;
    }
}
