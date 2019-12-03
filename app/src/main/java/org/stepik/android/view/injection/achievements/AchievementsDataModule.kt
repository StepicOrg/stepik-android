package org.stepik.android.view.injection.achievements

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.di.AppSingleton
import org.stepik.android.data.achievement.repository.AchievementRepositoryImpl
import org.stepik.android.data.achievement.source.AchievementRemoteDataSource
import org.stepik.android.domain.achievement.repository.AchievementRepository
import org.stepik.android.remote.achievement.AchievementRemoteDataSourceImpl
import org.stepik.android.remote.achievement.service.AchievementsService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class AchievementsDataModule {
    @Binds
    @AppSingleton
    internal abstract fun bindAchievementsRepository(
        achievementsRepositoryImpl: AchievementRepositoryImpl
    ): AchievementRepository

    @Binds
    internal abstract fun bindAchievementsRemoteDataSource(
        achievementsRemoteDataSourceImpl: AchievementRemoteDataSourceImpl
    ): AchievementRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideAchievementService(@Authorized retrofit: Retrofit): AchievementsService =
            retrofit.create(AchievementsService::class.java)
    }
}