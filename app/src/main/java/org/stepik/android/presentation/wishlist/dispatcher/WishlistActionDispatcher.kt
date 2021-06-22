package org.stepik.android.presentation.wishlist.dispatcher

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.interactor.CourseListUserInteractor
import org.stepik.android.domain.course_list.model.UserCourseQuery
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.domain.wishlist.interactor.WishlistInteractor
import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.presentation.wishlist.WishlistFeature
import org.stepik.android.view.injection.course_list.WishlistOperationBus
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class WishlistActionDispatcher
@Inject
constructor(
    private val wishlistInteractor: WishlistInteractor,
    private val courseListUserInteractor: CourseListUserInteractor,
    @WishlistOperationBus
    private val wishlistOperationObservable: Observable<WishlistOperationData>,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<WishlistFeature.Action, WishlistFeature.Message>() {
    init {
        compositeDisposable += wishlistOperationObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { onNewMessage(WishlistFeature.Message.WishlistOperationUpdate(it)) },
                onError = emptyOnErrorStub
            )
    }
    override fun handleAction(action: WishlistFeature.Action) {
        when (action) {
            is WishlistFeature.Action.FetchWishList -> {
                compositeDisposable += zip(
                    courseListUserInteractor.getAllUserCourses(userCourseQuery = UserCourseQuery(page = 1, isArchived = false), sourceType = DataSourceType.REMOTE),
                    wishlistInteractor.getWishlist(dataSourceType = DataSourceType.REMOTE)
                )
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { (userCourses, wishlistEntity) ->
                            val enrolledCourseIds = userCourses.map(UserCourse::course).toSet()
                            val wishlistEntityCourses = wishlistEntity.courses.toSet()
                            val updatedWishlistCourses = (wishlistEntityCourses - enrolledCourseIds).toList()
                            val updatedWishlistEntity = wishlistEntity.copy(courses = updatedWishlistCourses)

                            onNewMessage(WishlistFeature.Message.FetchWishlistSuccess(updatedWishlistEntity))
                        },
                        onError = { onNewMessage(WishlistFeature.Message.FetchWishListError) }
                    )
            }

            is WishlistFeature.Action.SaveWishlistEntity -> {
                compositeDisposable += wishlistInteractor
                    .updateWishlist(action.wishlistEntity)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onError = emptyOnErrorStub
                    )
            }
        }
    }
}