package org.stepic.droid.di

import dagger.Binds
import dagger.Module
import org.stepic.droid.base.Client
import org.stepic.droid.base.ClientImpl
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.base.ListenerContainerImpl
import org.stepic.droid.core.updatingstep.UpdatingStepPosterImpl
import org.stepic.droid.core.updatingstep.contract.UpdatingStepListener
import org.stepic.droid.core.updatingstep.contract.UpdatingStepPoster

@Module
interface AppStepModule {
    @Binds
    @AppSingleton
    fun bindsClient(clientImpl: ClientImpl<UpdatingStepListener>): Client<UpdatingStepListener>

    @Binds
    @AppSingleton
    fun bindContainer(listenerContainer: ListenerContainerImpl<UpdatingStepListener>): ListenerContainer<UpdatingStepListener>

    @Binds
    @AppSingleton
    fun bindsPoster(posterImpl: UpdatingStepPosterImpl): UpdatingStepPoster

}
