package org.stepic.droid.ui.activities


import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.branch.referral.Branch
import io.branch.referral.BranchError
import org.json.JSONObject
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.BranchParams
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.SplashPresenter
import org.stepic.droid.core.presenters.contracts.SplashView
import org.stepic.droid.util.AppConstants
import org.stepik.android.view.routing.deeplink.BranchRoute
import java.util.Arrays
import javax.inject.Inject

class SplashActivity : BackToExitActivityBase(), SplashView {

    @Inject
    internal lateinit var splashPresenter: SplashPresenter

    @Inject
    internal lateinit var analytics: Analytic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.componentManager().splashComponent().inject(this)
        splashPresenter.attachView(this)
//        if (!isTaskRoot) {
//            finish()
//            return
//        }

        defineShortcuts()
    }

    private fun defineShortcuts() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return
        }
        val shortcutManager = getSystemService(ShortcutManager::class.java) ?: return

        val catalogIntent = screenManager.getCatalogIntent(applicationContext)
        catalogIntent.action = AppConstants.OPEN_SHORTCUT_CATALOG
        val catalogShortcut = createShortcutInfo(AppConstants.CATALOG_SHORTCUT_ID, R.string.catalog_title, catalogIntent, R.drawable.ic_shortcut_find_courses)

        val profileIntent = screenManager.getMyProfileIntent(applicationContext)
        profileIntent.action = AppConstants.OPEN_SHORTCUT_PROFILE
        val profileShortcut = createShortcutInfo(AppConstants.PROFILE_SHORTCUT_ID, R.string.profile_title, profileIntent, R.drawable.ic_shortcut_profile)

        shortcutManager.dynamicShortcuts = Arrays.asList(catalogShortcut, profileShortcut)
    }

    @TargetApi(25)
    private fun createShortcutInfo(id: String, @StringRes titleRes: Int, catalogIntent: Intent, @DrawableRes iconRes: Int): ShortcutInfo {
        val title = getString(titleRes)
        return ShortcutInfo.Builder(this, id)
                .setShortLabel(title)
                .setLongLabel(title)
                .setIcon(Icon.createWithResource(this, iconRes))
                .setIntent(catalogIntent)
                .build()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onStart() {
        super.onStart()
        Branch.getInstance().initSession({ referringParams: JSONObject?, error: BranchError? ->
            if (error == null && referringParams != null && referringParams.has(BranchParams.FIELD_CAMPAIGN)) {
                analytics.reportAmplitudeEvent(AmplitudeAnalytic.Branch.LINK_OPENED, mapOf(
                    AmplitudeAnalytic.Branch.PARAM_CAMPAIGN to referringParams[BranchParams.FIELD_CAMPAIGN],
                    AmplitudeAnalytic.Branch.IS_FIRST_SESSION to referringParams.optBoolean(BranchParams.IS_FIRST_SESSION, false)
                ))
            }

            splashPresenter.onSplashCreated(referringParams)
        }, intent?.data, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        splashPresenter.detachView(this)
        if (isFinishing) {
            App.componentManager().releaseSplashComponent()
        }
    }

    override fun onShowLaunch() {
        screenManager.showLaunchFromSplash(this)
        finish()
    }

    override fun onShowHome() {
        screenManager.showMainFeedFromSplash(this)
        finish()
    }

    override fun onShowCatalog() {
        screenManager.showMainFeed(this, MainFeedActivity.CATALOG_INDEX)
    }

    override fun onShowOnboarding() {
        screenManager.showOnboarding(this)
        finish()
    }

    override fun onDeepLinkRoute(route: BranchRoute) {
        screenManager.openDeepLink(this, route)
        finish()
    }
}
