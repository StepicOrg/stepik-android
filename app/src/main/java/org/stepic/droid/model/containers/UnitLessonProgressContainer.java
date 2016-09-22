package org.stepic.droid.model.containers;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Unit;

import java.util.List;
import java.util.Map;

public class UnitLessonProgressContainer {
    List<Unit> unitList;
    List<Lesson> lessonList;
    Map<Long, Progress> unitProgressMap;

    public UnitLessonProgressContainer(List<Unit> unitList, List<Lesson> lessonList, Map<Long, Progress> unitProgressMap) {
        this.unitList = unitList;
        this.lessonList = lessonList;
        this.unitProgressMap = unitProgressMap;
    }

    public List<Unit> getUnitList() {
        return unitList;
    }

    public List<Lesson> getLessonList() {
        return lessonList;
    }

    public Map<Long, Progress> getUnitProgressMap() {
        return unitProgressMap;
    }
}
