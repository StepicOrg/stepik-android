package org.stepic.droid.util

import android.app.Activity
import androidx.annotation.StringRes
import androidx.core.app.Fragment
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.dialogs.LogoutAreYouSureDialog
import org.stepic.droid.viewmodel.ProfileSettingsViewModel


@StringRes
private val settingsTitleRes = R.string.settings_title

@StringRes
private val downloadsTitleRes = R.string.downloads

@StringRes
private val feedbackTitleRes = R.string.feedback_title

@StringRes
private val aboutTitleRes = R.string.about_app_title

@StringRes
private val logoutTitleRes = R.string.logout_title

object ProfileSettingsHelper {

    fun getProfileSettings(): List<ProfileSettingsViewModel> {
        val list = ArrayList<ProfileSettingsViewModel>(8)

        list.add(ProfileSettingsViewModel(settingsTitleRes))
        list.add(ProfileSettingsViewModel(downloadsTitleRes))
        list.add(ProfileSettingsViewModel(feedbackTitleRes))
        list.add(ProfileSettingsViewModel(aboutTitleRes))
        list.add(ProfileSettingsViewModel(logoutTitleRes, R.color.new_logout_color))

        return list
    }
}

fun ProfileSettingsViewModel?.clickProfileSettings(activity: Activity,
                                                   screenManager: ScreenManager,
                                                   fragment: Fragment,
                                                   analytic: Analytic) {
    if (this == null) {
        return
    }

    when (this.stringRes) {
        settingsTitleRes -> {
            analytic.reportEvent(Analytic.Screens.USER_OPEN_SETTINGS)
            screenManager.showSettings(activity)
        }

        downloadsTitleRes -> {
            analytic.reportEvent(Analytic.Screens.USER_OPEN_DOWNLOADS)
            screenManager.showDownloads(activity)
        }

        feedbackTitleRes -> {
            analytic.reportEvent(Analytic.Screens.USER_OPEN_FEEDBACK)
            screenManager.openFeedbackActivity(activity)
        }

        aboutTitleRes -> {
            analytic.reportEvent(Analytic.Screens.USER_OPEN_ABOUT_APP)
            screenManager.openAboutActivity(activity)
        }

        logoutTitleRes -> {
            analytic.reportEvent(Analytic.Screens.USER_LOGOUT)
            val dialog = LogoutAreYouSureDialog.newInstance()
            if (!dialog.isAdded) {
                dialog.show(fragment.childFragmentManager, null)
            }
        }

    }

}


