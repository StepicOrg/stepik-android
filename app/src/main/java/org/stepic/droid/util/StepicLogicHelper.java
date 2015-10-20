package org.stepic.droid.util;

import org.stepic.droid.model.Unit;

import java.util.List;

public class StepicLogicHelper {
    public static long[] fromUnitsToLessonIds(List<Unit> units) {
        long[] lessonsIds = new long[units.size()];
        for (int i = 0; i < units.size(); i++) {
            Unit unit = units.get(i);
            if (unit != null) {
                lessonsIds[i] = unit.getLesson();
            }
        }
        return lessonsIds;
    }
}
