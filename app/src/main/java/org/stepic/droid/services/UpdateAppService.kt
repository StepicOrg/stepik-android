package org.stepic.droid.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import com.squareup.otto.Bus
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.MainApplication
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.events.updating.NeedUpdateEvent
import org.stepic.droid.model.AppInfo
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.DeviceInfoUtil
import org.stepic.droid.web.IApi
import javax.inject.Inject

class UpdateAppService : IntentService("update_stepic") {

    @Inject
    lateinit var mainHandler: MainHandler;

    @Inject
    lateinit var configs: IConfig;

    @Inject
    lateinit var api: IApi;

    @Inject
    lateinit var bus: Bus;

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper

    @Inject
    lateinit var analytic: Analytic

    override fun onHandleIntent(intent: Intent?) {
        try {
            val lastShown = sharedPreferencesHelper.lastShownUpdatingMessageTimestamp
            val needUpdate = DateTimeHelper.isNeededUpdate(lastShown)
            if (needUpdate) {
                checkUpdateAndPushMessageOnMainFeed()
            }
        } catch (t: Throwable) {
            analytic.reportError(Analytic.Error.ERROR_UPDATE_CHECK_APP, t)
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
                mainHandler.post { bus.post(NeedUpdateEvent(linkForUpdate, isAppInGp)) }
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