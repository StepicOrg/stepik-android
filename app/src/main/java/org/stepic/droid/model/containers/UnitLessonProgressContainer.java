package org.stepic.droid.model.containers;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Unit;

import java.util.List;
import java.util.Map;

public class UnitLessonProgressContainer {
    List<Unit> mUnitList;
    List<Lesson> mLessonList;
    Map<Long, Progress> mUnitProgressMap;

    public UnitLessonProgressContainer(List<Unit> unitList, List<Lesson> lessonList, Map<Long, Progress> unitProgressMap) {
        mUnitList = unitList;
        mLessonList = lessonList;
        mUnitProgressMap = unitProgressMap;
    }

    public List<Unit> getUnitList() {
        return mUnitList;
    }

    public List<Lesson> getLessonList() {
        return mLessonList;
    }

    public Map<Long, Progress> getUnitProgressMap() {
        return mUnitProgressMap;
    }
}
