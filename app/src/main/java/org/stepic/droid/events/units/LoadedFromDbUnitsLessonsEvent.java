package org.stepic.droid.events.units;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;

import java.util.List;

public class LoadedFromDbUnitsLessonsEvent {
    List<Unit> units;
    List<Lesson> lessons;
    private Section section;

    public LoadedFromDbUnitsLessonsEvent (List<Unit> units, List<Lesson> lessons, Section section) {
        this.units = units;
        this.lessons = lessons;
        this.section = section;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public Section getSection() {
        return section;
    }
}
