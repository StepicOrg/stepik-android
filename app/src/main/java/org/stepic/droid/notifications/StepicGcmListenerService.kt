package org.stepic.droid.notifications

import android.os.Bundle
import com.google.android.gms.gcm.GcmListenerService
import com.google.gson.Gson
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.base.MainApplication
import org.stepic.droid.core.IShell
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.web.IApi
import javax.inject.Inject

class StepicGcmListenerService : GcmListenerService() {

    @Inject
    lateinit var notificationManager: INotificationManager

    @Inject
    lateinit var mShell : IShell

    @Inject
    lateinit var api : IApi

    init {
        MainApplication.component().inject(this)
    }

    override fun onMessageReceived(from: String?, data: Bundle?) {
        val notificationRawString: String? = data?.getString("object")
        try {
            val userId = mShell.getSharedPreferenceHelper().profile.id
            val userIdServerString = data?.getString("user_id")?:""
            val userIdServer = Integer.parseInt(userIdServerString)
            if (userIdServer.toLong() != userId){
                return;
            }
            val stepicNotification = Gson().fromJson(notificationRawString, Notification::class.java)
            stepicNotification?.let {
                notificationManager.showNotification(it)
            }
        } catch(e: Exception) {
            YandexMetrica.reportError("notification error parse", e);
        }
    }

}