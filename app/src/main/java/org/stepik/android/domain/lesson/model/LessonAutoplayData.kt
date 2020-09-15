package org.stepik.android.domain.lesson.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LessonAutoplayData(
    val lessonId: Long,
    val stepPosition: Int
) : Parcelable