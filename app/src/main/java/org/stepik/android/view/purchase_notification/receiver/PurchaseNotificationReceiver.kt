package org.stepik.android.view.purchase_notification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import javax.inject.Inject

class PurchaseNotificationReceiver : BroadcastReceiver() {
    companion object {
        const val REQUEST_CODE = 12303
        const val NOTIFICATION_DISMISSED = "notification_dismissed"

        fun createIntent(context: Context, action: String): Intent =
            Intent(context, PurchaseNotificationReceiver::class.java)
                .setAction(action)
    }

    @Inject
    lateinit var analytic: Analytic

    init {
        App.component().inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            NOTIFICATION_DISMISSED ->
                analytic.reportEvent(Analytic.Notification.PURCHASE_NOTIFICATION_DISMISSED)
        }
    }
}