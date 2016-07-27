package org.stepic.droid.core

import android.support.annotation.StringRes
import org.stepic.droid.R


enum class DeadlineType(@StringRes val deadlineTitle: Int) {
    softDeadline(R.string.soft_deadline_section),
    hardDeadline(R.string.hard_deadline_section)
}