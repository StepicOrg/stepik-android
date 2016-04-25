package org.stepic.droid.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import com.squareup.otto.Bus
import org.stepic.droid.base.MainApplication
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.model.AppInfo
import org.stepic.droid.web.IApi
import javax.inject.Inject

class UpdateAppService : IntentService("update_stepic") {

    @Inject
    lateinit var mainHandler: IMainHandler;

    @Inject
    lateinit var configs: IConfig;

    @Inject
    lateinit var api: IApi;

    @Inject
    lateinit var bus: Bus;

    override fun onHandleIntent(intent: Intent?) {
        checkUpdateAndPushMessageOnMainFeed()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MainApplication.component().inject(this)
        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }

    private fun checkUpdateAndPushMessageOnMainFeed() {
        if (configs.isCustomUpdateEnable) {
            val appInfo = api.infoForUpdating.execute()?.body()?.appInfo
            val currentCustomVersion = configs.oldUpdatingVersion

            if (appInfo?.custom_version ?: 0 > currentCustomVersion) {
                //need update
                val linkForUpdate = getLinkForUpdating(appInfo)
                val jjj = 0

            }
        } else {
            return;
        }
    }

    private fun getLinkForUpdating(appInfo: AppInfo?): String? {
        val links = appInfo?.download_links

        //should take from system arch, and update using this info
        if (links != null) {
            for (linkItem in links) {
                if (linkItem.architecture ?: "" == "all") {
                    return linkItem.link
                }
            }
        }

        return null
    }
}