package org.stepic.droid.core;

import android.content.Intent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;

import javax.inject.Singleton;

@Singleton
public interface ShareHelper {
    Intent getIntentForCourseSharing(@NotNull Course mCourse);

    Intent getIntentForShareCertificate(@NotNull CertificateViewItem certificateViewItem);

    Intent getIntentForStepSharing(Step step, Lesson lesson, @Nullable Unit unit);
}
