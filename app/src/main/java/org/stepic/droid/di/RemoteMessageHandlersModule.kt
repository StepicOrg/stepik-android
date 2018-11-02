package org.stepic.droid.di

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.stepic.droid.notifications.handlers.AchievementsRemoteMessageHandler
import org.stepic.droid.notifications.handlers.RemoteMessageHandler

@Module
abstract class RemoteMessageHandlersModule {

    @Module
    companion object {
        @Provides
        @IntoMap
        @StringKey(AchievementsRemoteMessageHandler.MESSAGE_TYPE)
        @JvmStatic
        fun provideAchievementsRemoteMessageHandler(): RemoteMessageHandler =
                AchievementsRemoteMessageHandler()
    }
}