package org.stepic.droid.core

import org.stepic.droid.core.components.MainFeedComponent
import org.stepic.droid.core.modules.MainFeedModule
import org.stepic.droid.di.AppCoreComponent
import org.stepic.droid.di.login.LoginComponent

class ComponentManagerImpl(private val appCoreComponent: AppCoreComponent) : ComponentManager {

    private val loginComponentMap = HashMap<String, LoginComponent>()

    override fun releaseLoginComponent(tag: String) {
        loginComponentMap.remove(tag)
    }

    override fun loginComponent(tag: String) =
            loginComponentMap.getOrPut(tag) {
                appCoreComponent
                        .loginComponentBuilder()
                        .build()
            }

    private var mainFeedComponentProp: MainFeedComponent? = null

    override fun mainFeedComponent(): MainFeedComponent {
        synchronized(this) {
            if (mainFeedComponentProp == null) {
                mainFeedComponentProp = appCoreComponent.plus(MainFeedModule())
            }
            return mainFeedComponentProp!!
        }
    }

    override fun releaseMainFeedComponent() {
        synchronized(this) {
            mainFeedComponentProp = null
        }
    }
}
