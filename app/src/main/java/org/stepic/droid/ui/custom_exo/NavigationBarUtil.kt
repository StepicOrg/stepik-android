package org.stepic.droid.ui.custom_exo

import android.app.Activity
import android.view.View
import android.view.WindowManager
import org.stepic.droid.util.AndroidDevices
import org.stepic.droid.util.hasCominationBar
import org.stepic.droid.util.isJellyBeanOrLater
import org.stepic.droid.util.isKitKatOrLater


object NavigationBarUtil {

    fun hideNavigationBar(needHide: Boolean = true, activity: Activity?) {
        if (activity == null) {
            return
        }
        var visibility = 0
        var navigationBar = 0

        if (isJellyBeanOrLater()) {
            visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            navigationBar = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
        if (needHide) {
            activity.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            @Suppress("DEPRECATION")
            navigationBar = navigationBar or View.SYSTEM_UI_FLAG_LOW_PROFILE
            if (!hasCominationBar(activity.applicationContext)) {
                navigationBar = navigationBar or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                if (isKitKatOrLater()) {
                    visibility = visibility or View.SYSTEM_UI_FLAG_IMMERSIVE
                }
                if (isJellyBeanOrLater()) {
                    visibility = visibility or View.SYSTEM_UI_FLAG_FULLSCREEN
                }
            }
        } else {
            activity.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            @Suppress("DEPRECATION")
            visibility = visibility or View.SYSTEM_UI_FLAG_VISIBLE
        }

        if (AndroidDevices.hasNavBar()) {
            visibility = visibility or navigationBar
        }
        activity.window?.decorView?.systemUiVisibility = visibility
    }
}