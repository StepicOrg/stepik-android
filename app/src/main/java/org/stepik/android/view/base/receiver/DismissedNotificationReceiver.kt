package org.stepik.android.view.base.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.base.analytic.ParcelableAnalyticEvent
import org.stepik.android.domain.streak.analytic.StreakNotificationDismissed
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class DismissedNotificationReceiver : BroadcastReceiver() {
    companion object {
        const val REQUEST_CODE = 13202

        private const val NOTIFICATION_DISMISSED = "notification_dismissed"
        private const val EXTRA_PARCELABLE_ANALYTIC_EVENT = "parcelable_analytic_event"

        fun createIntent(context: Context, analyticEvent: ParcelableAnalyticEvent): Intent =
            Intent(context, DismissedNotificationReceiver::class.java)
                .setAction(NOTIFICATION_DISMISSED)
                .putExtra(EXTRA_PARCELABLE_ANALYTIC_EVENT, analyticEvent)
    }

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var threadPool: ThreadPoolExecutor

    @Inject
    lateinit var sharedPreferences: SharedPreferenceHelper

    init {
        App.component().inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == NOTIFICATION_DISMISSED) {
            val analyticEvent = intent.getParcelableExtra<ParcelableAnalyticEvent>(EXTRA_PARCELABLE_ANALYTIC_EVENT)
            if (analyticEvent != null) {
                analytic.report(analyticEvent)
            }
            if (analyticEvent is StreakNotificationDismissed) {
                threadPool.execute {
                    sharedPreferences.resetNumberOfStreakNotifications()
                }
            }
        }
    }
}