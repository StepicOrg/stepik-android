package org.stepic.droid.di.routing

import dagger.Binds
import dagger.Module
import org.stepic.droid.core.RoutingConsumer
import org.stepic.droid.core.RoutingConsumerImpl
import org.stepic.droid.core.RoutingPoster
import org.stepic.droid.core.RoutingPosterImpl

@Module
interface RoutingModule {
    @Binds
    @RoutingScope
    fun bindsRoutingConsumer(routingConsumerImpl: RoutingConsumerImpl): RoutingConsumer

    @Binds
    @RoutingScope
    fun bindsRoutingPoster(routingPosterImpl: RoutingPosterImpl): RoutingPoster
}
