package org.stepik.android.view.course_content.model

import android.support.annotation.StringRes
import java.util.Date

data class CourseContentSectionDate(
    @StringRes
    val titleRes: Int,
    val date: Date
)