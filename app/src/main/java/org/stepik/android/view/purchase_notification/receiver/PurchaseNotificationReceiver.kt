package org.stepik.android.view.purchase_notification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepik.android.domain.purchase_notification.analytic.PurchaseNotificationDismissed
import javax.inject.Inject

class PurchaseNotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val NOTIFICATION_DISMISSED = "notification_dismissed"
        private const val EXTRA_COURSE_ID = "course_id"

        const val REQUEST_CODE = 12303

        fun createIntent(context: Context, courseId: Long): Intent =
            Intent(context, PurchaseNotificationReceiver::class.java)
                .setAction(NOTIFICATION_DISMISSED)
                .putExtra(EXTRA_COURSE_ID, courseId)
    }

    @Inject
    lateinit var analytic: Analytic

    init {
        App.component().inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            NOTIFICATION_DISMISSED -> {
                val courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1)
                analytic.report(PurchaseNotificationDismissed(courseId))
            }
        }
    }
}