package org.stepic.droid.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import org.stepic.droid.R
import org.stepic.droid.features.stories.ui.activity.StoriesActivity
import org.stepic.droid.ui.activities.PhotoViewActivity
import org.stepic.droid.ui.activities.SplashActivity
import org.stepik.android.view.auth.ui.activity.CredentialAuthActivity
import org.stepik.android.view.auth.ui.activity.RegistrationActivity
import org.stepik.android.view.auth.ui.activity.SocialAuthActivity
import org.stepik.android.view.video_player.ui.activity.VideoPlayerActivity
import java.util.WeakHashMap

/** Keeps ordinary Activity content clear of system bars on Android 15 and newer. */
object ActivitySafeZone : Application.ActivityLifecycleCallbacks {
    // At this luminance, black and white icons have equal contrast against the background.
    private const val DARK_ICON_LUMINANCE_THRESHOLD = 0.179
    private val installedViews = WeakHashMap<View, Boolean>()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // Auth screens animate their own insets; the player manages fullscreen controls separately.
        if (isHandledSeparately(activity)) return

        val content = activity.findViewById<View>(android.R.id.content) ?: return
        if (installedViews.put(content, true) != null) return

        setBarIconContrast(activity)
        val statusBarBackground = addStatusBarBackground(activity)

        val initialLeft = content.paddingLeft
        val initialTop = content.paddingTop
        val initialRight = content.paddingRight
        val initialBottom = content.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val safeInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val backgroundParams = statusBarBackground.layoutParams
            if (backgroundParams.height != safeInsets.top) {
                backgroundParams.height = safeInsets.top
                statusBarBackground.layoutParams = backgroundParams
            }
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
    override fun onActivityResumed(activity: Activity) {
        if (!isHandledSeparately(activity)) setBarIconContrast(activity)
    }
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    private fun isHandledSeparately(activity: Activity): Boolean =
        activity is CredentialAuthActivity ||
            activity is RegistrationActivity ||
            activity is VideoPlayerActivity

    private fun usesBlackBars(activity: Activity): Boolean =
        activity is SplashActivity ||
            activity is StoriesActivity ||
            activity is PhotoViewActivity

    private fun statusBarBackgroundColor(activity: Activity): Int =
        ContextCompat.getColor(
            /* context = */ activity,
            /* id = */ when {
                usesBlackBars(activity) -> R.color.black
                else -> R.color.color_status_bar
            }
        )

    private fun addStatusBarBackground(activity: Activity): View {
        // On Android 15+ with edge-to-edge, setStatusBarColor and the theme attribute have no effect.
        // Draw behind the transparent status bar; the content background also covers its padding.
        // This view has no touch or accessibility behavior and never consumes insets.
        val background = View(activity).apply {
            setBackgroundColor(statusBarBackgroundColor(activity))
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        (activity.window.decorView as FrameLayout).addView(
            background,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 0, Gravity.TOP)
        )
        return background
    }

    private fun navigationBarBackgroundColor(activity: Activity): Int =
        ContextCompat.getColor(
            /* context = */ activity,
            /* id = */ if (usesBlackBars(activity)) R.color.black else R.color.color_background
        )

    private fun setBarIconContrast(activity: Activity) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        controller.isAppearanceLightStatusBars =
            ColorUtils.calculateLuminance(statusBarBackgroundColor(activity)) > DARK_ICON_LUMINANCE_THRESHOLD
        controller.isAppearanceLightNavigationBars =
            ColorUtils.calculateLuminance(navigationBarBackgroundColor(activity)) > DARK_ICON_LUMINANCE_THRESHOLD
    }
}
