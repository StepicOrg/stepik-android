package org.stepic.droid.events.units;

import org.stepic.droid.model.Section;

public class UnitLessonSavedEvent {
    private Section mSection;

    public UnitLessonSavedEvent(Section mSection) {


        this.mSection = mSection;
    }

    public Section getmSection() {
        return mSection;
    }
}
