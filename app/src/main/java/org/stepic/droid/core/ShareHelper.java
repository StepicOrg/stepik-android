package org.stepic.droid.core;

import android.content.Intent;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.Course;

public interface ShareHelper {
     Intent getIntentForCourseSharing(@NotNull Course mCourse);
}
