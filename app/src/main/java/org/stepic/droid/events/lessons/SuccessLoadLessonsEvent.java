package org.stepic.droid.events.lessons;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;

import java.util.List;

public class SuccessLoadLessonsEvent {
    private Section section;
    private List<Lesson> lessons;
    private List<Unit> units;

    public SuccessLoadLessonsEvent(Section section, List<Lesson> lessons, List<Unit> units) {
        this.section = section;
        this.lessons = lessons;
        this.units = units;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public Section getSection() {
        return section;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }
}
