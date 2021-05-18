package org.stepik.android.view.course_content.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.model.Progress
import org.stepik.android.model.Section

@Parcelize
data class RequiredSection(
    val section: Section,
    val progress: Progress
) : Parcelable