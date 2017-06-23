package org.stepic.droid.core

import android.telephony.PhoneStateListener
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class MyExoPhoneStateListener
@Inject constructor
(private val mainHandler: MainHandler) : PhoneStateListener() {

    interface Callback {
        fun onIncomingCall()
    }

    var callback: Callback? = null

    override fun onCallStateChanged(state: Int, incomingNumber: String?) {
        if (state == 1) {
            mainHandler.post { callback?.onIncomingCall() }
        }
    }

    fun subscribe(callback: Callback) {
        this.callback = callback
    }

    fun unsubscribe() {
        this.callback = null
    }
}
