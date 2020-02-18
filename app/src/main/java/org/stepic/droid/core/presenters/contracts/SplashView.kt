package org.stepic.droid.core.presenters.contracts

import org.stepik.android.view.routing.deeplink.BranchRoute

interface SplashView {

    fun onShowLaunch()

    fun onShowHome()

    fun onShowCatalog()

    fun onShowOnboarding()

    fun onDeepLinkRoute(route: BranchRoute)
}
