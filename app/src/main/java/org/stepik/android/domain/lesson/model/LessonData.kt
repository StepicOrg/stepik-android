package org.stepik.android.domain.lesson.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.model.Course
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Unit

@Parcelize
data class LessonData(
    val lesson: Lesson,
    val unit: Unit?,
    val section: Section?,
    val course: Course?,

    val stepPosition: Int = 0,
    val discussionId: Long? = null,
    val discussionThread: String? = null
) : Parcelable
