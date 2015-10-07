package org.stepic.droid.events.units;

import org.stepic.droid.model.Section;

public class FailureLoadUnitsEvent {
    private Section mSection;

    public FailureLoadUnitsEvent(Section mSection) {

        this.mSection = mSection;
    }

    public Section getmSection() {
        return mSection;
    }
}
