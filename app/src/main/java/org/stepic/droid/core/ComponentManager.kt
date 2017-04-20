package org.stepic.droid.core

import android.support.annotation.MainThread
import org.stepic.droid.di.login.LoginComponent
import org.stepic.droid.di.mainscreen.MainScreenComponent
import org.stepic.droid.di.routing.RoutingComponent

// TODO: 16.03.17 make more generic solution, for every component handling
interface ComponentManager {
    fun mainFeedComponent(): MainScreenComponent

    fun releaseMainFeedComponent()

    fun loginComponent(tag: String): LoginComponent

    fun releaseLoginComponent(tag: String)


    @MainThread
    fun routingComponent(): RoutingComponent

    @MainThread
    fun releaseRoutingComponent()
}
