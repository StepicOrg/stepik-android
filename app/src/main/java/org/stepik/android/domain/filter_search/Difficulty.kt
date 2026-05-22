package org.stepik.android.domain.filter_search

import androidx.annotation.StringRes
import org.stepic.droid.R

enum class Difficulty(val queryName: String, @StringRes val stringResId: Int) {
    Easy("easy", R.string.filter_dialog_level_easy),
    Normal("normal", R.string.filter_dialog_level_normal),
    Hard("hard", R.string.filter_dialog_level_hard)
}