package org.stepic.droid.util;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Course;
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

    public static long[] fromVideosToStepIds(List<CachedVideo> cachedVideos) {
        long[] stepIds = new long[cachedVideos.size()];
        for (int i = 0; i < cachedVideos.size(); i++) {
            CachedVideo video = cachedVideos.get(i);
            if (video != null) {
                stepIds[i] = video.getStepId();
            }
        }
        return stepIds;
    }

    @NotNull
    public static String getPathForCourseOrEmpty(Course course, IConfig config) {
        return course.getCover() == null ? "" : config.getBaseUrl() + course.getCover();
    }
}
