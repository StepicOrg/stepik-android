package org.stepik.android.view.injection.achievements

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.achievement.AchievementsPresenter
import org.stepik.android.presentation.base.injection.ViewModelKey

@Module
internal abstract class AchievementsModule {

    /**
     * PRESENTATION LAYER
     */
    @Binds
    @IntoMap
    @ViewModelKey(AchievementsPresenter::class)
    internal abstract fun bindAchievementsPresenter(achievementsPresenter: AchievementsPresenter): ViewModel
}