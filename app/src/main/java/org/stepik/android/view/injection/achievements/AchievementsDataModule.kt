package org.stepik.android.view.injection.achievements

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.di.AppSingleton
import org.stepik.android.data.achievements.repository.AchievementsRepositoryImpl
import org.stepik.android.data.achievements.source.AchievementsRemoteDataSource
import org.stepik.android.domain.achievements.repository.AchievementsRepository
import org.stepik.android.remote.achievements.AchievementsRemoteDataSourceImpl
import org.stepik.android.remote.achievements.service.AchievementsService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class AchievementsDataModule {
    @Binds
    @AppSingleton
    internal abstract fun bindAchievementsRepository(
        achievementsRepositoryImpl: AchievementsRepositoryImpl
    ): AchievementsRepository

    @Binds
    internal abstract fun bindAchievementsRemoteDataSource(
        achievementsRemoteDataSourceImpl: AchievementsRemoteDataSourceImpl
    ): AchievementsRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideAchievementService(@Authorized retrofit: Retrofit): AchievementsService =
            retrofit.create(AchievementsService::class.java)
    }
}