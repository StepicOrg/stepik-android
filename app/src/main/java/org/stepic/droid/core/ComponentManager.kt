package org.stepic.droid.core

import org.stepic.droid.di.login.LoginComponent
import org.stepic.droid.di.mainscreen.MainScreenComponent

// TODO: 16.03.17 make more generic solution, for every component handling
interface ComponentManager {
    fun mainFeedComponent(): MainScreenComponent

    fun releaseMainFeedComponent()

    fun loginComponent(tag: String): LoginComponent

    fun releaseLoginComponent(tag: String)
}
