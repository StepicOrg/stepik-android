package org.stepik.android.view.injection.attempts

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.attempts.AttemptsPresenter
import org.stepik.android.presentation.base.injection.ViewModelKey

@Module
abstract class AttemptsModule {
    /**
     * PRESENTATION LAYER
     */

    @Binds
    @IntoMap
    @ViewModelKey(AttemptsPresenter::class)
    internal abstract fun bindAttemptPresenter(attemptsPresenter: AttemptsPresenter): ViewModel
}