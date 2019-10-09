package org.stepik.android.view.injection.course_reviews

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_reviews.ComposeCourseReviewPresenter

@Module
abstract class ComposeCourseReviewModule {
    /**
     * PRESENTATION LAYER
     */

    @Binds
    @IntoMap
    @ViewModelKey(ComposeCourseReviewPresenter::class)
    internal abstract fun bindComposeCourseReviewPresenter(composeCourseReviewPresenter: ComposeCourseReviewPresenter): ViewModel
}