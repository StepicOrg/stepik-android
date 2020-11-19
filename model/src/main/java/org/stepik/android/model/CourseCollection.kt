package org.stepik.android.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.nobird.android.core.model.Identifiable

@Parcelize
data class CourseCollection(
    override val id: Long,
    val position: Int,
    val title: String,
    val language: String,
    val courses: List<Long>,
    val description: String,
    val platform: Int
) : Parcelable, Identifiable<Long>