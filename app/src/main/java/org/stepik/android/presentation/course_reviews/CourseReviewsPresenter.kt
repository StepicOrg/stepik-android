package org.stepik.android.presentation.course_reviews

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.CourseId
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_reviews.interactor.CourseReviewsInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class CourseReviewsPresenter
@Inject
constructor(
    @CourseId
    private val courseId: Long,

    private val courseReviewsInteractor: CourseReviewsInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CourseReviewsView>() {

    private var state: CourseReviewsView.State = CourseReviewsView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    init {
        fetchCourseReviews()
    }

    override fun attachView(view: CourseReviewsView) {
        super.attachView(view)
        view.setState(state)
    }

    private fun fetchCourseReviews() {
        if (state != CourseReviewsView.State.Idle) return

        state = CourseReviewsView.State.Loading
        subscribeForCourseReviews()
    }

    private fun subscribeForCourseReviews(shouldSkipStoredValue: Boolean = false) {
        compositeDisposable += courseReviewsInteractor
            .getCourseReviewItems(courseId)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { reviews ->
                    state =
                        if (reviews.isNotEmpty()) {
                            CourseReviewsView.State.CourseReviewsCache(reviews)
                        } else {
                            CourseReviewsView.State.EmptyContent
                        }
                },
                onError = { state = CourseReviewsView.State.NetworkError }
            )
    }
}