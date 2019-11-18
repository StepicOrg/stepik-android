package org.stepik.android.view.injection.submission

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.submission.SubmissionsPresenter

@Module
internal abstract class SubmissionModule {

    /**
     * PRESENTATION LAYER
     */
    @Binds
    @IntoMap
    @ViewModelKey(SubmissionsPresenter::class)
    internal abstract fun bindSubmissionPresenter(submissionPresenter: SubmissionsPresenter): ViewModel
}