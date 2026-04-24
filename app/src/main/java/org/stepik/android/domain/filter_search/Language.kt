package org.stepik.android.domain.filter_search

import androidx.annotation.StringRes
import org.stepic.droid.R
enum class Language(val langCode: String, @StringRes val stringResId: Int) {
    Any("", R.string.filter_dialog_language_any),
    Russian("ru", R.string.filter_dialog_language_rus),
    English("en", R.string.filter_dialog_language_english)
}