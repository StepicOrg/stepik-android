package org.stepic.droid.core

import android.telephony.PhoneStateListener
import com.squareup.otto.Bus
import org.stepic.droid.base.App
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.events.IncomingCallEvent
import javax.inject.Inject

class MyPhoneStateListener : PhoneStateListener() {
    init {
        App.component().inject(this)
    }

    @Inject
    lateinit var mBus: Bus

    @Inject
    lateinit var mHandler: MainHandler

    override fun onCallStateChanged(state: Int, incomingNumber: String?) {
        if (state == 1) {
            mHandler.post { mBus.post(IncomingCallEvent()) }
        }
    }
}
