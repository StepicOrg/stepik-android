package org.stepic.droid.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import org.joda.time.DateTime
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.notifications.RescheduleChecker
import org.stepic.droid.notifications.StepikNotificationManager
import org.stepic.droid.storage.operations.DatabaseFacade
import javax.inject.Inject

class RescheduleService : IntentService("RescheduleService") {
    companion object {
        val requestCode = 181

        val notificationIdKey = "notificationIdKey"
    }

    @Inject
    lateinit var stepikNotificationManager: StepikNotificationManager

    @Inject
    lateinit var rescheduleChecker: RescheduleChecker

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var databaseFacade: DatabaseFacade


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        App.component().inject(this)
        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }

    override fun onHandleIntent(intent: Intent?) {
        //it is background thread
        val notificationId: Long = intent?.getLongExtra(notificationIdKey, -1) ?: -1

        val now = DateTime.now()
        val isNeedReschedule = rescheduleChecker.isRescheduleNeed(now.millis) //just check, but not really reschedule
        if (isNeedReschedule) {
            analytic.reportEventWithName(Analytic.Notification.NEED_RESCHEDULE_BUT_ALREADY_RESCHEDULED, now.toString())
        }

        if (notificationId < 0) {
            return
        }

        val notification = databaseFacade.getNotificationById(notificationId)
        notification?.let {
            stepikNotificationManager.showNotification(it)
        }

    }

}

