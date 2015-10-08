package org.stepic.droid.events.units;

import org.stepic.droid.model.Section;

public class FailureLoadEvent {
    private Section mSection;

    public FailureLoadEvent(Section mSection) {

        this.mSection = mSection;
    }

    public Section getmSection() {
        return mSection;
    }
}
