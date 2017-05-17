package org.stepic.droid.di.routing

import dagger.Binds
import dagger.Module
import org.stepic.droid.base.Client
import org.stepic.droid.base.ClientImpl
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.base.ListenerContainerImpl
import org.stepic.droid.core.routing.RoutingPosterImpl
import org.stepic.droid.core.routing.contract.RoutingListener
import org.stepic.droid.core.routing.contract.RoutingPoster

@Module
interface RoutingModule {
    @Binds
    @RoutingScope
    fun bindsRoutingClient(routingConsumerImpl: ClientImpl<RoutingListener>): Client<RoutingListener>

    @Binds
    @RoutingScope
    fun bindListenerContainer(routingListenerContainer: ListenerContainerImpl<RoutingListener>): ListenerContainer<RoutingListener>

    @Binds
    @RoutingScope
    fun bindsRoutingPoster(routingPosterImpl: RoutingPosterImpl): RoutingPoster

}
