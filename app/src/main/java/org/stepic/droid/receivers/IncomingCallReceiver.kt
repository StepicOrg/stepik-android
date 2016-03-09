package org.stepic.droid.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.squareup.otto.Bus
import org.stepic.droid.base.MainApplication
import org.stepic.droid.concurrency.IMainHandler
import javax.inject.Inject

class IncomingCallReceiver() : BroadcastReceiver() {
    init {
        MainApplication.component().inject(this)
    }

    @Inject
    lateinit var mBus: Bus

    @Inject
    lateinit var mHandler: IMainHandler

    override fun onReceive(p0: Context?, p1: Intent?) {
        mHandler.post { mBus.post(IncomingCallReceiver()) }
    }

}
