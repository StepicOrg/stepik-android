package org.stepic.droid.util

import android.app.Activity
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import org.stepic.droid.R
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.dialogs.LogoutAreYouSureDialog
import org.stepic.droid.viewmodel.ProfileSettingsViewModel


@StringRes
private val settingTitleRes = R.string.settings_title

@StringRes
private val downloadsTitleRes = R.string.downloads

@StringRes
private val notificationsTitleRes = R.string.notification_title

@StringRes
private val feedbackTitleRes = R.string.feedback_title

@StringRes
private val aboutTitleRes = R.string.about_app_title

@StringRes
private val logoutTitleRes = R.string.logout_title

object ProfileSettingsHelper {

    fun getProfileSettings(): List<ProfileSettingsViewModel> {
        val list = ArrayList<ProfileSettingsViewModel>(8)

        list.add(ProfileSettingsViewModel(settingTitleRes))
        list.add(ProfileSettingsViewModel(downloadsTitleRes))
        list.add(ProfileSettingsViewModel(notificationsTitleRes))
        list.add(ProfileSettingsViewModel(feedbackTitleRes))
        list.add(ProfileSettingsViewModel(aboutTitleRes))
        list.add(ProfileSettingsViewModel(logoutTitleRes, R.color.new_logout_color))

        return list
    }
}

fun ProfileSettingsViewModel?.clickProfileSettings(activity: Activity,
                                                   screenManager: ScreenManager,
                                                   fragment: Fragment) {
    if (this == null) {
        return
    }

    when (this.stringRes) {
        settingTitleRes -> {
            screenManager.showSettings(activity)
        }

        downloadsTitleRes -> {
            screenManager.showDownloads(activity)
        }

        notificationsTitleRes -> {
            screenManager.showNotifications(activity)
        }

        feedbackTitleRes -> {
            screenManager.openFeedbackActivity(activity)
        }

        aboutTitleRes -> {
            screenManager.openAboutActivity(activity)
        }

        logoutTitleRes -> {
            //// TODO: 16.08.17 add analytic event
            val dialog = LogoutAreYouSureDialog.newInstance()
            if (!dialog.isAdded) {
                dialog.show(fragment.childFragmentManager, null)
            }
        }

    }

}


