package org.stepic.droid.events.units;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.model.containers.UnitLessonProgressContainer;

import java.util.List;
import java.util.Map;

public class LoadedFromDbUnitsLessonsEvent {
    private Section section;
    private UnitLessonProgressContainer mUnitLessonProgressContainer;

    public LoadedFromDbUnitsLessonsEvent(UnitLessonProgressContainer unitLessonProgressContainer, Section section) {
        mUnitLessonProgressContainer = unitLessonProgressContainer;
        this.section = section;
    }

    public List<Unit> getUnits() {
        return mUnitLessonProgressContainer.getUnitList();
    }

    public List<Lesson> getLessons() {
        return mUnitLessonProgressContainer.getLessonList();
    }

    public Map<Long, Progress> getProgressMap() {
        return mUnitLessonProgressContainer.getUnitProgressMap();
    }

    public Section getSection() {
        return section;
    }
}
