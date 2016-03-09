package org.stepic.droid.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.squareup.otto.Bus
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.base.MainApplication
import org.stepic.droid.concurrency.IMainHandler
import javax.inject.Inject

class IncomingCallReceiver() : BroadcastReceiver() {

    override fun onReceive(context: Context?, p1: Intent?) {
        try {
            val tmgr = context?.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            tmgr?.listen(MyStatePhoneListener(), PhoneStateListener.LISTEN_CALL_STATE)
        } catch (ex: Exception) {
            YandexMetrica.reportError("IncomingCallReceiver", ex)
        }

    }

    class MyStatePhoneListener : PhoneStateListener() {

        init {
            MainApplication.component().inject(this)
        }

        @Inject
        lateinit var mBus: Bus

        @Inject
        lateinit var mHandler: IMainHandler

        override fun onCallStateChanged(state: Int, incomingNumber: String?) {
            if (state == 1) {
                mHandler.post { mBus.post(IncomingCallReceiver()) }
            }
        }
    }
}
