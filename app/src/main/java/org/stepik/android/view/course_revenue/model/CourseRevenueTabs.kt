package org.stepik.android.view.course_revenue.model

import androidx.annotation.StringRes
import org.stepic.droid.R

enum class CourseRevenueTabs(@StringRes val titleStringRes: Int) {
    COURSE_BENEFITS(R.string.course_benefits_tab),
    COURSE_BENEFITS_MONTHLY(R.string.course_benefits_monthly_tab)
}