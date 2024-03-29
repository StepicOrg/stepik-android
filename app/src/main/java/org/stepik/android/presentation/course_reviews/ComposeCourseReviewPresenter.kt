package org.stepik.android.presentation.course_reviews

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_reviews.analytic.CourseReviewCreatedAnalyticEvent
import org.stepik.android.domain.course_reviews.analytic.CourseReviewUpdatedAnalyticEvent
import org.stepik.android.domain.course_reviews.interactor.ComposeCourseReviewInteractor
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.user_reviews.model.UserCourseReviewOperation
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.view.injection.user_reviews.UserCourseReviewOperationBus
import javax.inject.Inject

class ComposeCourseReviewPresenter
@Inject
constructor(
    private val analytic: Analytic,
    private val composeCourseReviewInteractor: ComposeCourseReviewInteractor,

    @UserCourseReviewOperationBus
    private val userCourseReviewOperationSubject: PublishSubject<UserCourseReviewOperation>,
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

    fun createCourseReview(courseReview: CourseReview, courseReviewViewSource: String) {
        val courseReviewSource = composeCourseReviewInteractor
            .createCourseReview(courseReview)
            .doOnSuccess {
                analytic.report(
                    CourseReviewCreatedAnalyticEvent(
                        rating = it.score,
                        courseId = it.course,
                        source = courseReviewViewSource
                    )
                )
                userCourseReviewOperationSubject.onNext(UserCourseReviewOperation.CreateReviewOperation(it))
            }
        replaceCourseReview(courseReviewSource)
    }

    fun updateCourseReview(oldCourseReview: CourseReview, newCourseReview: CourseReview, courseReviewViewSource: String) {
        val courseReviewSource = composeCourseReviewInteractor
            .updateCourseReview(newCourseReview)
            .doOnSuccess {
                analytic.report(
                    CourseReviewUpdatedAnalyticEvent(
                        fromRating = oldCourseReview.score,
                        toRating = it.score,
                        courseId = it.course,
                        source = courseReviewViewSource
                    )
                )
                userCourseReviewOperationSubject.onNext(UserCourseReviewOperation.EditReviewOperation(it))
            }
        replaceCourseReview(courseReviewSource)
    }

    private fun replaceCourseReview(courseReviewSource: Single<CourseReview>) {
        state = ComposeCourseReviewView.State.Loading
        compositeDisposable += courseReviewSource
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { state = ComposeCourseReviewView.State.Complete(it) },
                onError = { state = ComposeCourseReviewView.State.Idle; view?.showNetworkError() }
            )
    }
}