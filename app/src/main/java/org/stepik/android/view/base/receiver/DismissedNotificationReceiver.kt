package org.stepik.android.view.base.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.base.analytic.BUNDLEABLE_ANALYTIC_EVENT
import org.stepik.android.domain.base.analytic.toGenericAnalyticEvent
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class DismissedNotificationReceiver : BroadcastReceiver() {
    companion object {
        const val REQUEST_CODE = 13202

        private const val NOTIFICATION_DISMISSED = "notification_dismissed"
        private const val STREAK_NOTIFICATION_DISMISSED = "streak_notification_dismissed"

        fun createIntent(context: Context, bundleableAnalyticEvent: Bundle): Intent =
            Intent(context, DismissedNotificationReceiver::class.java)
                .setAction(NOTIFICATION_DISMISSED)
                .putExtra(BUNDLEABLE_ANALYTIC_EVENT, bundleableAnalyticEvent)

        fun createStreakNotificationIntent(context: Context, bundleableAnalyticEvent: Bundle): Intent =
            Intent(context, DismissedNotificationReceiver::class.java)
                .setAction(STREAK_NOTIFICATION_DISMISSED)
                .putExtra(BUNDLEABLE_ANALYTIC_EVENT, bundleableAnalyticEvent)
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
        when (action) {
            NOTIFICATION_DISMISSED ->
                logAnalyticEvent(intent)
            STREAK_NOTIFICATION_DISMISSED -> {
                logAnalyticEvent(intent)
                threadPool.execute {
                    sharedPreferences.resetNumberOfStreakNotifications()
                }
            }
        }
    }

    private fun logAnalyticEvent(intent: Intent) {
        val analyticEvent = intent
            .getBundleExtra(BUNDLEABLE_ANALYTIC_EVENT)
            ?.toGenericAnalyticEvent()

        if (analyticEvent != null) {
            analytic.report(analyticEvent)
        }
    }
}