package org.stepic.droid.di

import dagger.Binds
import dagger.Module
import org.stepic.droid.base.Client
import org.stepic.droid.base.ClientImpl
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.base.ListenerContainerImpl
import org.stepic.droid.core.filters.FiltersPosterImpl
import org.stepic.droid.core.filters.contract.FiltersListener
import org.stepic.droid.core.filters.contract.FiltersPoster

@Module
interface AppFiltersModule {

    @Binds
    @AppSingleton
    fun bindsFiltersPoster(filtersPosterImpl: FiltersPosterImpl): FiltersPoster

    @Binds
    @AppSingleton
    fun bindsFiltersListeners(container: ListenerContainerImpl<FiltersListener>): ListenerContainer<FiltersListener>

    @Binds
    @AppSingleton
    fun bindsFiltersClient(container: ClientImpl<FiltersListener>): Client<FiltersListener>
}
