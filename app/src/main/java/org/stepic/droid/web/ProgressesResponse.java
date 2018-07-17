package org.stepic.droid.web;

import org.stepic.droid.model.Progress;
import org.stepik.android.model.Meta;

import java.util.List;

public class ProgressesResponse extends MetaResponseBase {

    List<Progress> progresses;

    public ProgressesResponse(Meta meta, List<Progress> progresses) {
        super(meta);
        this.progresses = progresses;
    }

    public List<Progress> getProgresses() {
        return progresses;
    }
}
