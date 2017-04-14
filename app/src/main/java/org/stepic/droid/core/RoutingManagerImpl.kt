package org.stepic.droid.core

import org.stepic.droid.di.routing.RoutingScope
import org.stepic.droid.model.Section
import javax.inject.Inject

@RoutingScope
class RoutingManagerImpl @Inject constructor() : RoutingManager {

    val listeners: MutableSet<RoutingManager.Listener> = HashSet()

    override fun subscribe(listener: RoutingManager.Listener) {
        listeners.add(listener)
    }

    override fun unsubscribe(listener: RoutingManager.Listener) {
        listeners.remove(listener)
    }

    override fun onSectionChanged(oldSection: Section, newSection: Section) {
        listeners.forEach {
            it.onSectionChanged(oldSection, newSection)
        }
    }
}
