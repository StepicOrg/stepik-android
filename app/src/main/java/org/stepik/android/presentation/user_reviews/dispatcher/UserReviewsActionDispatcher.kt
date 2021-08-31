package org.stepik.android.presentation.user_reviews.dispatcher

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.base.DataSourceType
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
                compositeDisposable += userCourseReviewsInteractor
                    .fetchUserCourseReviewItems(primaryDataSourceType = DataSourceType.REMOTE)
                    .ignoreElement()
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy()
            }

            is UserReviewsFeature.Action.ListenForUserReviews -> {
                compositeDisposable += userCourseReviewsInteractor
                    .getUserCourseReviewItems()
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onNext = { onNewMessage(UserReviewsFeature.Message.FetchUserReviewsSuccess(it)) },
                        onError = { onNewMessage(UserReviewsFeature.Message.FetchUserReviewsError) }
                    )
            }

            is UserReviewsFeature.Action.PublishChanges -> {
                compositeDisposable += userCourseReviewsInteractor
                    .publishChanges(action.userCourseReviewsResult)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onError = emptyOnErrorStub
                    )
            }

            is UserReviewsFeature.Action.DeleteReview -> {
                compositeDisposable += userCourseReviewsInteractor
                    .removeCourseReview(action.courseReview)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onComplete = { onNewMessage(UserReviewsFeature.Message.DeletedReviewUserReviewsSuccess(action.courseReview)) },
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