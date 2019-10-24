package org.stepic.droid.notifications.model

import android.app.NotificationManager
import android.os.Build
import androidx.annotation.StringRes
import org.stepic.droid.R

private fun getImportanceCompat(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager.IMPORTANCE_HIGH
        } else {
            //it is -1 because channel should not be used for previous versions
            -1
        }

private val commentId = "commentChannel"
private val reviewId = "reviewChannel"
private val teachId = "teachChannel"
private val learnId = "learnChannel"
private val otherId = "otherChannel"
private val userId = "userChannel"

enum class StepikNotificationChannel
(
        val channelId: String,
        @StringRes
        val visibleChannelNameRes: Int,
        @StringRes
        val visibleChannelDescriptionRes: Int,
        val importance: Int = getImportanceCompat()) {
    //order is important!

    comments(commentId, R.string.comments_channel_name, R.string.comments_channel_description),
    review(reviewId, R.string.review_channel_name, R.string.review_channel_description),
    teach(teachId, R.string.teach_channel_name, R.string.teach_channel_description),
    learn(learnId, R.string.learn_channel_name, R.string.learn_channel_description),
    other(otherId, R.string.other_channel_name, R.string.other_channel_name),
    user(userId, R.string.user_channel_name, R.string.user_channel_description)
}
