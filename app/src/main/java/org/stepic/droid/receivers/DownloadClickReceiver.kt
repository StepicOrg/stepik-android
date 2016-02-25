package org.stepic.droid.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import org.stepic.droid.base.MainApplication
import org.stepic.droid.core.IScreenManager
import javax.inject.Inject

class DownloadClickReceiver : BroadcastReceiver() {

    @Inject
    lateinit var mScreenProvider : IScreenManager

    init {
        MainApplication.component().inject(this)
    }

    override fun onReceive(context: Context, intent: Intent) = mScreenProvider.showDownload()
}
