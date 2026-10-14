package org.stepic.droid.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.stepik.android.view.auth.ui.activity.CredentialAuthActivity
import org.stepik.android.view.auth.ui.activity.RegistrationActivity
import org.stepik.android.view.auth.ui.activity.SocialAuthActivity
import org.stepik.android.view.video_player.ui.activity.VideoPlayerActivity
import java.util.WeakHashMap

/** Keeps ordinary Activity content clear of system bars on Android 15 and newer. */
object ActivitySafeZone : Application.ActivityLifecycleCallbacks {
    private val installedViews = WeakHashMap<View, Boolean>()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // Auth screens animate their own insets; the player manages fullscreen controls separately.
        if (activity is CredentialAuthActivity ||
            activity is RegistrationActivity ||
            activity is VideoPlayerActivity
        ) {
            return
        }

        val content = activity.findViewById<View>(android.R.id.content) ?: return
        if (installedViews.put(content, true) != null) return

        val initialLeft = content.paddingLeft
        val initialTop = content.paddingTop
        val initialRight = content.paddingRight
        val initialBottom = content.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val safeInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(
                initialLeft + safeInsets.left,
                initialTop + safeInsets.top,
                initialRight + safeInsets.right,
                initialBottom + safeInsets.bottom
            )
            insets
        }
        if (ViewCompat.isAttachedToWindow(content)) {
            ViewCompat.requestApplyInsets(content)
        }
    }

    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
