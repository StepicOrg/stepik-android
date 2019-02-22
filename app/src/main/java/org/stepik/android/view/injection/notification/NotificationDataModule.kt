package org.stepik.android.view.injection.notification

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.notification.NotificationCacheDataSourceImpl
import org.stepik.android.data.notification.repository.NotificationRepositoryImpl
import org.stepik.android.data.notification.source.NotificationCacheDataSource
import org.stepik.android.data.notification.source.NotificationRemoteDataSource
import org.stepik.android.domain.notification.repository.NotificationRepository
import org.stepik.android.remote.notification.NotificationRemoteDataSourceImpl

@Module
abstract class NotificationDataModule {
    @Binds
    internal abstract fun bindLessonRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    internal abstract fun bindNotificationCacheDataSource(
        notificationCacheDataSourceImpl: NotificationCacheDataSourceImpl
    ): NotificationCacheDataSource

    @Binds
    internal abstract fun bindNotificationRemoteDataSource(
        notificationRemoteDataSourceImpl: NotificationRemoteDataSourceImpl
    ): NotificationRemoteDataSource
}