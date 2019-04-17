package org.stepik.android.presentation.course_reviews

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_reviews.interactor.ComposeCourseReviewInteractor
import org.stepik.android.domain.course_reviews.model.CourseReview
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
    private var state: ComposeCourseReviewView.State = ComposeCourseReviewView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: ComposeCourseReviewView) {
        super.attachView(view)
        view.setState(state)
    }

    fun createCourseReview(courseReview: CourseReview) {
        replaceCourseReview(composeCourseReviewInteractor.createCourseReview(courseReview))
    }

    fun updateCourseReview(courseReview: CourseReview) {
        replaceCourseReview(composeCourseReviewInteractor.updateCourseReview(courseReview))
    }

    private fun replaceCourseReview(courseReviewSource: Single<CourseReview>) {
        compositeDisposable += courseReviewSource
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { state = ComposeCourseReviewView.State.Complete(it) },
                onError = { state = ComposeCourseReviewView.State.Idle; view?.showNetworkError() }
            )
    }
}