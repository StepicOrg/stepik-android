package org.stepic.droid.di.routing

import dagger.Binds
import dagger.Module
import org.stepic.droid.core.RoutingManager
import org.stepic.droid.core.RoutingManagerImpl

@Module
interface RoutingModule {
    @Binds
    @RoutingScope
    fun bindsRoutingManager(routingManagerImpl: RoutingManagerImpl): RoutingManager
}
