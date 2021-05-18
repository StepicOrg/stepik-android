package org.stepik.android.domain.lesson.model

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.stepik.android.model.Course
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepik.android.view.course_content.model.RequiredSection

@Parcelize
data class LessonData(
    val lesson: Lesson,
    val unit: Unit?,
    val section: Section?,
    val course: Course?,

    val stepPosition: Int = 0,
    val discussionId: Long? = null,
    val discussionThread: String? = null,
    val requiredSection: RequiredSection? = null
) : Parcelable {
    @IgnoredOnParcel
    val isDemo: Boolean =
        course?.enrollment == 0L && course.isPaid && lesson.actions?.learnLesson != null
}
