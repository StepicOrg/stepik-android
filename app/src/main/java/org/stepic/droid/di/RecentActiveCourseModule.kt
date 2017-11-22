package org.stepic.droid.di

import dagger.Binds
import dagger.Module
import org.stepic.droid.base.Client
import org.stepic.droid.base.ClientImpl
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.base.ListenerContainerImpl
import org.stepic.droid.core.earlystreak.EarlyStreakPosterImpl
import org.stepic.droid.core.earlystreak.contract.EarlyStreakListener
import org.stepic.droid.core.earlystreak.contract.EarlyStreakPoster

@Module
interface RecentActiveCourseModule {

    @Binds
    @AppSingleton
    fun bindPoster(earlyStreakPoster: EarlyStreakPosterImpl): EarlyStreakPoster

    @Binds
    @AppSingleton
    fun bindListenerContainer(container: ListenerContainerImpl<EarlyStreakListener>): ListenerContainer<EarlyStreakListener>

    @Binds
    @AppSingleton
    fun bindClient(container: ClientImpl<EarlyStreakListener>): Client<EarlyStreakListener>

}
