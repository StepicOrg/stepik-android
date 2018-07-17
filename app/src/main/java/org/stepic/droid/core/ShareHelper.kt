package org.stepic.droid.core

import android.content.Intent
import org.stepic.droid.model.*
import org.stepik.android.model.structure.Unit
import org.stepik.android.model.structure.Course

interface ShareHelper {
    fun getIntentForCourseSharing(course: Course): Intent

    fun getIntentForShareCertificate(certificateViewItem: CertificateViewItem): Intent

    fun getIntentForStepSharing(step: Step, lesson: Lesson, unit: Unit?): Intent

    fun getIntentForSectionSharing(section: Section): Intent

    fun getIntentForProfileSharing (userViewModel: UserViewModel) : Intent
}
