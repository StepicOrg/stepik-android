package org.stepik.android.view.injection.solutions

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.solutions.SolutionsPresenter

@Module
abstract class SolutionsModule {
    /**
     * PRESENTATION LAYER
     */

    @Binds
    @IntoMap
    @ViewModelKey(SolutionsPresenter::class)
    internal abstract fun bindSolutionsPresenter(solutionsPresenter: SolutionsPresenter): ViewModel
}