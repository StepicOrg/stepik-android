package org.stepic.droid.util;

import android.net.Uri;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;

import org.stepic.droid.R;
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


    public static DraweeController getControllerForCourse(Course course, IConfig mConfig){
        if (course.getCover() != null) {
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(mConfig.getBaseUrl() + course.getCover())
                    .setAutoPlayAnimations(true)
                    .build();
            return controller;
        } else {
            //for empty cover:
            Uri uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                    .path(String.valueOf(R.drawable.ic_course_placeholder))
                    .build();

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(uri)
                    .setAutoPlayAnimations(true)
                    .build();
            return controller;
        }
    }
}
