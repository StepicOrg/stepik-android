package org.stepic.droid.di

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.stepic.droid.notifications.handlers.AchievementsRemoteMessageHandler
import org.stepic.droid.notifications.handlers.RemoteMessageHandler

@Module
class RemoteMessageHandlersModule {
    @Provides
    @IntoMap
    @StringKey(AchievementsRemoteMessageHandler.MESSAGE_TYPE)
    internal fun provideAchievementsRemoteMessageHandler(): RemoteMessageHandler =
            AchievementsRemoteMessageHandler()
}