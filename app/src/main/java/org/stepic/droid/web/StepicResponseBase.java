package org.stepic.droid.web;

import org.stepic.droid.model.Meta;

public abstract class StepicResponseBase implements IStepicResponse{

    private Meta meta;

    public StepicResponseBase(Meta meta)
    {
        this.meta = meta;
    }

    public Meta getMeta() {
        return meta;
    }
}
