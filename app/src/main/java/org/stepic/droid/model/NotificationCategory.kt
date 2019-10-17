package org.stepic.droid.model

import androidx.annotation.StringRes
import org.stepic.droid.R

enum class NotificationCategory(@StringRes val title: Int) {
    all(R.string.all_title),
    learn(R.string.learning_title),
    comments(R.string.comments_title),
    review(R.string.reviews_title),
    teach(R.string.teaching_title),
    default(R.string.other_title)
}