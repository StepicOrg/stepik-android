package org.stepik.android.presentation.course_reviews

import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_reviews.interactor.ComposeCourseReviewInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ComposeCourseReviewPresenter
@Inject
constructor(
    private val composeCourseReviewInteractor: ComposeCourseReviewInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ComposeCourseReviewView>() {


}