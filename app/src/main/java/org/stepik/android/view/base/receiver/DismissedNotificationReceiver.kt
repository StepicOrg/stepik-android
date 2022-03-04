package org.stepik.android.view.base.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.base.analytic.BundleableAnalyticEvent
import org.stepik.android.domain.base.analytic.toGenericAnalyticEvent
import timber.log.Timber
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class DismissedNotificationReceiver : BroadcastReceiver() {
    companion object {
        const val REQUEST_CODE = 13202

        private const val NOTIFICATION_DISMISSED = "notification_dismissed"

        fun createIntent(context: Context, bundleableAnalyticEvent: Bundle): Intent =
            Intent(context, DismissedNotificationReceiver::class.java)
                .setAction(NOTIFICATION_DISMISSED)
                .putExtra(BundleableAnalyticEvent.BUNDLEABLE_ANALYTIC_EVENT, bundleableAnalyticEvent)
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
            val analyticEvent = intent
                .getBundleExtra(BundleableAnalyticEvent.BUNDLEABLE_ANALYTIC_EVENT)
                ?.toGenericAnalyticEvent()

            if (analyticEvent != null) {
                Timber.tag("APPS!").d("Dismissed - Event name: ${analyticEvent.name} Event params: ${analyticEvent.params}")
                analytic.report(analyticEvent)
            }
            // TODO Handle this case
//            if (analyticEvent?.name == StreakNotificationDismissed) {
//                threadPool.execute {
//                    sharedPreferences.resetNumberOfStreakNotifications()
//                }
//            }
        }
    }
}