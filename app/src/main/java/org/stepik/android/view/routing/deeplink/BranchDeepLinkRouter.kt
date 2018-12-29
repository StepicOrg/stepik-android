package org.stepik.android.view.routing.deeplink

import android.content.Context
import org.stepic.droid.core.ScreenManager

interface BranchDeepLinkRouter {
    /**
     * Handles branch route
     * @return true if route was successfully handled
     */
    fun handleBranchRoute(screenManager: ScreenManager, context: Context, route: BranchRoute): Boolean
}