package org.stepic.droid.core

import android.telephony.PhoneStateListener
import com.squareup.otto.Bus
import org.stepic.droid.base.MainApplication
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.events.IncomingCallEvent
import javax.inject.Inject

class MyStatePhoneListener: PhoneStateListener() {

    init {
        MainApplication.component().inject(this)
    }

    @Inject
    lateinit var mBus: Bus

    @Inject
    lateinit var mHandler: IMainHandler

    override fun onCallStateChanged(state: Int, incomingNumber: String?) {
        if (state == 1) {
            mHandler.post { mBus.post(IncomingCallEvent()) }
        }
    }
}