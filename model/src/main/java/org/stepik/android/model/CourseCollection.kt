package org.stepik.android.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CourseCollection(
    val id: Long,
    val position: Int,
    val title: String,
    val language: String,
    val courses: List<Long>,
    val description: String
) : Parcelable