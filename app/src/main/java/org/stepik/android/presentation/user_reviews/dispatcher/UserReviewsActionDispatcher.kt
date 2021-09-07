package org.stepik.android.presentation.user_reviews.dispatcher

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_reviews.analytic.CourseReviewDeletedAnalyticEvent
import org.stepik.android.domain.course_reviews.analytic.CourseReviewViewSource
import org.stepik.android.domain.course_reviews.analytic.UserCourseReviewsScreenOpenedAnalyticEvent
import org.stepik.android.domain.profile.interactor.ProfileInteractor
import org.stepik.android.domain.user_reviews.interactor.UserCourseReviewsInteractor
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import org.stepik.android.domain.user_reviews.model.UserCourseReviewOperation
import org.stepik.android.model.Course
import org.stepik.android.presentation.user_reviews.UserReviewsFeature
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.user_reviews.UserCourseReviewOperationBus
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class UserReviewsActionDispatcher
@Inject
constructor(
    private val analytic: Analytic,
    private val profileInteractor: ProfileInteractor,
    private val userCourseReviewsInteractor: UserCourseReviewsInteractor,
    @UserCourseReviewOperationBus
    private val userCourseReviewOperationObservable: Observable<UserCourseReviewOperation>,
    @EnrollmentCourseUpdates
    private val enrollmentUpdatesObservable: Observable<Course>,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<UserReviewsFeature.Action, UserReviewsFeature.Message>() {
    init {
        subscribeForUserCourseReviewOperationUpdates()
        subscribeForEnrollmentUpdates()
    }
    override fun handleAction(action: UserReviewsFeature.Action) {
        when (action) {
            is UserReviewsFeature.Action.FetchUserReviews -> {
                compositeDisposable += Flowable
                    .fromArray(DataSourceType.CACHE, DataSourceType.REMOTE)
                    .concatMapSingle { sourceType ->
                        userCourseReviewsInteractor
                            .fetchUserCourseReviewItems(primaryDataSourceType = sourceType)
                            .map { userReviewsResult -> Result.success(userReviewsResult) }
                            .onErrorReturn { exception -> Result.failure(exception) }
                    }
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onNext = { result ->
                            val message =
                                result.fold(
                                    onSuccess = { UserReviewsFeature.Message.FetchUserReviewsSuccess(it) },
                                    onFailure = { UserReviewsFeature.Message.FetchUserReviewsError }
                                )
                            onNewMessage(message)
                        },
                        onError = { onNewMessage(UserReviewsFeature.Message.FetchUserReviewsError) }
                    )
            }

            is UserReviewsFeature.Action.LogScreenOpenedEvent -> {
                compositeDisposable += userCourseReviewsInteractor
                    .getAnalyticProfileData()
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = {
                            val state = if (it.isCurrentUser) {
                                AmplitudeAnalytic.Profile.Values.SELF
                            } else {
                                AmplitudeAnalytic.Profile.Values.OTHER
                            }
                            analytic.report(UserCourseReviewsScreenOpenedAnalyticEvent(state, it.user.id))
                        },
                        onError = emptyOnErrorStub
                    )
            }

            is UserReviewsFeature.Action.DeleteReview -> {
                compositeDisposable += userCourseReviewsInteractor
                    .removeCourseReview(action.courseReview)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onComplete = {
                            analytic.report(
                                CourseReviewDeletedAnalyticEvent(
                                    rating = action.courseReview.score,
                                    courseId = action.courseReview.course,
                                    source = CourseReviewViewSource.USER_REVIEWS_SOURCE
                                )
                            )
                            onNewMessage(UserReviewsFeature.Message.DeletedReviewUserReviewsSuccess(action.courseReview))
                        },
                        onError = { onNewMessage(UserReviewsFeature.Message.DeletedReviewUserReviewsError(action.courseReview)) }
                    )
            }

            is UserReviewsFeature.Action.FetchEnrolledCourseInfo -> {
                compositeDisposable += userCourseReviewsInteractor
                    .enrolledCourse(action.course)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { userCourseReviewItem ->
                        val message =
                            when (userCourseReviewItem) {
                                is UserCourseReviewItem.ReviewedItem ->
                                    UserReviewsFeature.Message.EnrolledReviewedCourseMessage(userCourseReviewItem)

                                is UserCourseReviewItem.PotentialReviewItem ->
                                    UserReviewsFeature.Message.EnrolledPotentialReviewMessage(userCourseReviewItem)

                                else ->
                                    throw IllegalArgumentException("Subtype of UserCourseReviewItem is not supported")
                            }
                            onNewMessage(message)
                        },
                        onError = emptyOnErrorStub
                    )
            }
        }
    }

    private fun subscribeForUserCourseReviewOperationUpdates() {
        compositeDisposable += userCourseReviewOperationObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { userCourseReviewOperation ->
                    val message =
                        when (userCourseReviewOperation) {
                            is UserCourseReviewOperation.CreateReviewOperation ->
                                UserReviewsFeature.Message.NewReviewSubmission(userCourseReviewOperation.courseReview)

                            is UserCourseReviewOperation.EditReviewOperation ->
                                UserReviewsFeature.Message.EditReviewSubmission(userCourseReviewOperation.courseReview)

                            is UserCourseReviewOperation.RemoveReviewOperation ->
                                UserReviewsFeature.Message.DeletedReviewSubmission(userCourseReviewOperation.courseReview)
                        }
                    onNewMessage(message)
                },
                onError = emptyOnErrorStub
            )
    }

    private fun subscribeForEnrollmentUpdates() {
        compositeDisposable += enrollmentUpdatesObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { onNewMessage(UserReviewsFeature.Message.EnrolledCourseMessage(it)) },
                onError = emptyOnErrorStub
            )
    }
}