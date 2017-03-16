package org.stepic.droid.core

import org.stepic.droid.core.components.AppCoreComponent
import org.stepic.droid.core.components.MainFeedComponent
import org.stepic.droid.core.modules.MainFeedModule

class ComponentManagerImpl(private val appCoreComponent: AppCoreComponent) : ComponentManager {

    private var mainFeedComponentProp: MainFeedComponent? = null

    override fun getMainFeedComponent(): MainFeedComponent {
        synchronized(this) {
            if (mainFeedComponentProp == null) {
                mainFeedComponentProp = appCoreComponent.plus(MainFeedModule())
            }
            return mainFeedComponentProp!!
        }
    }

    override fun removeMainFeedComponent() {
        synchronized(this) {
            mainFeedComponentProp = null
        }
    }
}
