package org.stepik.android.domain.filter_search

import androidx.annotation.StringRes
import org.stepic.droid.R
enum class Type(@StringRes val stringResId: Int) {
    Course(R.string.filter_dialog_type_course),
    Program(R.string.filter_dialog_type_program)
}