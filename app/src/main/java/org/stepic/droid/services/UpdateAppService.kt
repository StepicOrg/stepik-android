package org.stepic.droid.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import org.stepic.droid.base.MainApplication
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.web.IApi
import javax.inject.Inject

class UpdateAppService : IntentService("update_stepic") {

    @Inject
    lateinit var mainHandler: IMainHandler;

    @Inject
    lateinit var configs: IConfig;

    @Inject
    lateinit var api: IApi;

    override fun onHandleIntent(intent: Intent?) {
        checkUpdateAndPushMessageOnMainFeed()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MainApplication.component().inject(this)
        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }

    private fun checkUpdateAndPushMessageOnMainFeed() {
    }
}