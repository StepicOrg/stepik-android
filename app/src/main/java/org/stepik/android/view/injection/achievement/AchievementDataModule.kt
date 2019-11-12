package org.stepik.android.view.injection.achievement

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.features.achievements.repository.AchievementsRepository
import org.stepic.droid.features.achievements.repository.AchievementsRepositoryImpl
import org.stepik.android.remote.achievement.service.AchievementsService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class AchievementDataModule {
    @Binds
    @AppSingleton
    abstract fun bindAchievementsRepository(achievementsRepositoryImpl: AchievementsRepositoryImpl): AchievementsRepository

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideAchievementService(@Authorized retrofit: Retrofit): AchievementsService =
            retrofit.create(AchievementsService::class.java)
    }
}