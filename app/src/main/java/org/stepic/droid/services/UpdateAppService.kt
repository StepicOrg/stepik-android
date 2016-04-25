package org.stepic.droid.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import com.squareup.otto.Bus
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.base.MainApplication
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.model.AppInfo
import org.stepic.droid.util.AndroidDevices
import org.stepic.droid.util.DeviceInfoUtil
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
        try {
            checkUpdateAndPushMessageOnMainFeed()
        } catch (t: Throwable) {
            YandexMetrica.reportError("update check failed", t)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MainApplication.component().inject(this)
        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }

    private fun checkUpdateAndPushMessageOnMainFeed() {
        if (configs.isCustomUpdateEnable) {
            val appInfo = api.infoForUpdating?.app_info
            val currentVersion = DeviceInfoUtil.getBuildVersion(MainApplication.getAppContext());

            if (appInfo?.custom_version ?: 0 > currentVersion) {
                //need update
                val linkForUpdate = getLinkForUpdating(appInfo)
                val isAppInGp = appInfo?.is_app_in_gp ?: true
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