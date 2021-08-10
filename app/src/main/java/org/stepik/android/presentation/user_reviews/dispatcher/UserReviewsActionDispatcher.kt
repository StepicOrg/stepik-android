package org.stepik.android.presentation.user_reviews.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.user_reviews.interactor.UserCourseReviewsInteractor
import org.stepik.android.presentation.user_reviews.UserReviewsFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class UserReviewsActionDispatcher
@Inject
constructor(
    private val userCourseReviewsInteractor: UserCourseReviewsInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<UserReviewsFeature.Action, UserReviewsFeature.Message>() {
    override fun handleAction(action: UserReviewsFeature.Action) {
        when (action) {
            is UserReviewsFeature.Action.FetchUserReviews -> {
                compositeDisposable += userCourseReviewsInteractor
                    .fetchUserCourseReviewItems(primaryDataSourceType = DataSourceType.REMOTE)
                    .ignoreElement()
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
//                        onSuccess = { onNewMessage(UserReviewsFeature.Message.FetchUserReviewsSuccess(it)) },
//                        onError = { onNewMessage(UserReviewsFeature.Message.FetchUserReviewsError) }
                    )
            }

            is UserReviewsFeature.Action.ListenForUserReviews -> {
                compositeDisposable += userCourseReviewsInteractor
                    .getUserCourseReviewItems()
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(UserReviewsFeature.Message.FetchUserReviewsSuccess(it)) },
                        onError = { onNewMessage(UserReviewsFeature.Message.FetchUserReviewsError) }
                    )
            }

            is UserReviewsFeature.Action.PublishChanges -> {
                compositeDisposable += userCourseReviewsInteractor
                    .publishChanges(action.userCourseReviewsResult)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy()
            }
        }
    }
}