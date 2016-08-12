package org.stepic.droid.core;

import android.content.Intent;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.model.Course;

import javax.inject.Singleton;

@Singleton
public interface ShareHelper {
    Intent getIntentForCourseSharing(@NotNull Course mCourse);

    Intent getIntentForShareCertificate(@NotNull CertificateViewItem certificateViewItem);
}
