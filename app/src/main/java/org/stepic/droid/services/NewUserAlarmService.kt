package org.stepic.droid.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import org.stepic.droid.base.MainApplication
import org.stepic.droid.web.IApi
import javax.inject.Inject

class NewUserAlarmService : IntentService("NewUserAlarm") {
    companion object {
        var notificationTimestampSentKey = "notificationTimestampKey"
        var requestCode = 177
    }

    @Inject
    lateinit var api: IApi

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MainApplication.component().inject(this)
        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }

    override fun onHandleIntent(intent: Intent?) {
        api.sendFeedback("kir-maka@yandex.ru", "Не удаляйте этот тикет. Нужен, для оценки адекватности времени.") //can throw runtime exception
    }
}
