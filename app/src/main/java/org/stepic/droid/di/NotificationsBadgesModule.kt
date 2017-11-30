package org.stepic.droid.di

import dagger.Binds
import dagger.Module
import org.stepic.droid.base.Client
import org.stepic.droid.base.ClientImpl
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.base.ListenerContainerImpl
import org.stepic.droid.notifications.badges.NotificationsBadgesListener


@Module
interface NotificationsBadgesModule {

    @Binds
    @AppSingleton
    fun bindListenerContainer(container: ListenerContainerImpl<NotificationsBadgesListener>): ListenerContainer<NotificationsBadgesListener>

    @Binds
    @AppSingleton
    fun bindClient(container: ClientImpl<NotificationsBadgesListener>): Client<NotificationsBadgesListener>

}