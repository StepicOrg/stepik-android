package org.stepic.droid.core.presenters

import android.content.Context
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.configuration.Config
import org.stepic.droid.core.presenters.contracts.UpdateAppView
import org.stepic.droid.di.mainscreen.MainScreenScope
import org.stepic.droid.model.AppInfo
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.DeviceInfoUtil
import org.stepic.droid.web.Api
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@MainScreenScope
class UpdateAppPresenter
@Inject constructor(
        analytic: Analytic,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val config: Config,
        private val api: Api,
        private val context: Context /* todo provide build version instead of context*/)
    : PresenterWithPotentialLeak<UpdateAppView>(analytic) {

    //only once on main activity session
    private var wasChecked = false

    fun checkForUpdate() {
        if (wasChecked) {
            return
        }
        wasChecked = true

        threadPoolExecutor.execute {
            val lastShown = sharedPreferenceHelper.lastShownUpdatingMessageTimestamp
            val needUpdate = DateTimeHelper.isNeededUpdate(lastShown)
            if (needUpdate && config.isCustomUpdateEnable) {
                val appInfo = try {
                    api.infoForUpdating?.app_info
                } catch (exception: Exception) {
                    analytic.reportError(Analytic.Error.ERROR_UPDATE_CHECK_APP, exception)
                    return@execute
                }
                val currentVersion = DeviceInfoUtil.getBuildVersion(context);

                if (appInfo?.custom_version ?: 0 > currentVersion) {
                    //need update
                    val linkForUpdate = getLinkForUpdating(appInfo)
                    val isAppInGp = appInfo?.is_app_in_gp ?: true
                    mainHandler.post { view?.onNeedUpdate(linkForUpdate, isAppInGp) }
                }
            }
        }
    }

    private fun getLinkForUpdating(appInfo: AppInfo?): String? {
        val links = appInfo?.download_links ?: return null

        for ((architecture, link) in links) {
            if (architecture ?: "" == "all") {
                return link
            }
        }
        return null
    }

}
