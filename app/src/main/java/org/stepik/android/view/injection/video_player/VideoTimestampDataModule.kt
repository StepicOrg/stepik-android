package org.stepik.android.view.injection.video_player

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.video_player.VideoTimestampCacheDataSourceImpl
import org.stepik.android.data.video_player.repository.VideoTimestampRepositoryImpl
import org.stepik.android.data.video_player.source.VideoTimestampCacheDataSource
import org.stepik.android.domain.video_player.repository.VideoTimestampRepository

@Module
abstract class VideoTimestampDataModule {
    @Binds
    internal abstract fun bindVideoTimestampRepository(
        videoTimestampRepositoryImpl: VideoTimestampRepositoryImpl
    ): VideoTimestampRepository

    @Binds
    internal abstract fun bindVideoTimestampCacheDataSource(
        videoTimestampCacheDataSourceImpl: VideoTimestampCacheDataSourceImpl
    ): VideoTimestampCacheDataSource
}