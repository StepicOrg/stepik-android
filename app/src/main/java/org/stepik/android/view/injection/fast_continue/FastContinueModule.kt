package org.stepik.android.view.injection.fast_continue

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.fast_continue.FastContinuePresenter
import org.stepik.android.presentation.fast_continue.FastContinueView
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer

@Module
abstract class FastContinueModule {
    @Binds
    @IntoMap
    @ViewModelKey(FastContinuePresenter::class)
    internal abstract fun bindFastContinuePresenter(fastContinuePresenter: FastContinuePresenter): ViewModel

    @Binds
    internal abstract fun bindCourseContinueViewContainer(@FastContinueScope viewContainer: PresenterViewContainer<FastContinueView>): ViewContainer<out CourseContinueView>

    @Module
    companion object {
        @Provides
        @JvmStatic
        @FastContinueScope
        fun provideViewContainer(): PresenterViewContainer<FastContinueView> =
            DefaultPresenterViewContainer()
    }
}