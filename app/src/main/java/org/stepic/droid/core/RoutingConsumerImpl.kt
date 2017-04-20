package org.stepic.droid.core

import org.stepic.droid.di.routing.RoutingScope
import javax.inject.Inject

@RoutingScope
class RoutingConsumerImpl @Inject constructor(
        private val routingListenerContainer: RoutingListenerContainer
) : RoutingConsumer {

    override fun subscribe(listener: RoutingConsumer.Listener) {
        routingListenerContainer.add(listener)
    }

    override fun unsubscribe(listener: RoutingConsumer.Listener) {
        routingListenerContainer.remove (listener)
    }
}
