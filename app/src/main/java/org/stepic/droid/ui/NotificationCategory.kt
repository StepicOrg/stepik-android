package org.stepic.droid.ui

import android.support.annotation.StringRes
import org.stepic.droid.R

enum class NotificationCategory(@StringRes val title: Int) {
    all(R.string.all_title),
    learning(R.string.learning_title),
    comments(R.string.comments_title),
    review(R.string.reviews_title),
    teaching(R.string.teaching_title),
    other(R.string.other_title)
}
