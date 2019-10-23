package org.stepic.droid.core

import android.content.Intent
import org.stepic.droid.model.CertificateViewItem
import org.stepic.droid.model.UserViewModel
import org.stepik.android.model.Course
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Unit

interface ShareHelper {
    fun getIntentForCourseSharing(course: Course): Intent

    fun getIntentForShareCertificate(certificateViewItem: CertificateViewItem): Intent

    fun getIntentForStepSharing(step: Step, lesson: Lesson, unit: Unit?): Intent

    fun getIntentForSectionSharing(section: Section): Intent

    fun getIntentForProfileSharing(userViewModel: UserViewModel): Intent
}
