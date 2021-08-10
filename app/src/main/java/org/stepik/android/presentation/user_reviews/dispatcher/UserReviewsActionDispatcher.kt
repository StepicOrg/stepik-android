package org.stepik.android.presentation.user_reviews.dispatcher

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.user_reviews.interactor.UserCourseReviewsInteractor
import org.stepik.android.domain.user_reviews.model.UserCourseReviewOperation
import org.stepik.android.presentation.user_reviews.UserReviewsFeature
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
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<UserReviewsFeature.Action, UserReviewsFeature.Message>() {
    init {
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
                                UserReviewsFeature.Message.DeletedReview(userCourseReviewOperation.courseReview)
                    }
                    onNewMessage(message)
                },
                onError = emptyOnErrorStub
            )
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
                        onError = emptyOnErrorStub
                    )
            }
        }
    }
}