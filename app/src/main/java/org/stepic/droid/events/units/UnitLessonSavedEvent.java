package org.stepic.droid.events.units;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;

import java.util.List;

public class UnitLessonSavedEvent {
    private Section section;
    private final List<Unit> units;
    private final List<Lesson> lessons;

    public UnitLessonSavedEvent(Section section, List<Unit> units, List<Lesson> lessons) {


        this.section = section;
        this.units = units;
        this.lessons = lessons;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public Section getSection() {
        return section;
    }
}
