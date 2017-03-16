package org.stepic.droid.core

import org.stepic.droid.core.components.MainFeedComponent

// TODO: 16.03.17 make more generic solution, for every component handling
interface ComponentManager {
    fun getMainFeedComponent(): MainFeedComponent

    fun removeMainFeedComponent()
}
