package org.stepic.droid.core

import android.content.Intent
import org.stepic.droid.model.CertificateListItem
import org.stepik.android.model.*
import org.stepik.android.model.Unit
import org.stepik.android.model.user.User

interface ShareHelper {
    fun getIntentForCourseSharing(course: Course): Intent

    fun getIntentForShareCertificate(certificateListItem: CertificateListItem.Data): Intent

    fun getIntentForStepSharing(step: Step, lesson: Lesson, unit: Unit?): Intent

    fun getIntentForSectionSharing(section: Section): Intent

    fun getIntentForUserSharing(user: User): Intent

    fun getIntentForCourseResultSharing(course: Course, message: String): Intent

    fun getIntentForCourseResultCertificateSharing(certificate: Certificate, message: String): Intent
}
