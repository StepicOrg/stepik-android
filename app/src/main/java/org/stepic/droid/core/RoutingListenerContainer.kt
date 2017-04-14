package org.stepic.droid.core

import org.stepic.droid.di.routing.RoutingScope
import javax.inject.Inject

@RoutingScope
class RoutingListenerContainer @Inject constructor() {

    private val listeners: MutableSet<RoutingConsumer.Listener> = HashSet()

    fun add(listener: RoutingConsumer.Listener) {
        listeners.add(listener)
    }

    fun remove(listener: RoutingConsumer.Listener) {
        listeners.remove(listener)
    }

    fun iterator() = listeners.iterator()

}
