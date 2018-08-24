package org.stepic.droid.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.configuration.Config;
import org.stepik.android.model.Course;
import org.stepik.android.model.Unit;

import java.util.List;

public class StepikLogicHelper {
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

    @NotNull
    public static String getPathForCourseOrEmpty(@Nullable Course course, Config config) {
        if (course == null) {
            return "";
        }
        return course.getCover() == null ? "" : config.getBaseUrl() + course.getCover();
    }
}
