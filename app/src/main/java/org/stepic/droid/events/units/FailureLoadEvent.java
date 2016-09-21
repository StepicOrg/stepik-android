package org.stepic.droid.events.units;

import org.stepic.droid.model.Section;

public class FailureLoadEvent {
    private Section section;

    public FailureLoadEvent(Section section) {

        this.section = section;
    }

    public Section getSection() {
        return section;
    }
}
