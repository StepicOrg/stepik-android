package org.stepik.android.presentation.user_courses.dispatcher

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.presentation.user_courses.UserCoursesFeature
import org.stepik.android.view.injection.course_list.UserCoursesOperationBus
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class UserCoursesActionDispatcher
@Inject
constructor(
    @UserCoursesOperationBus
    userCourseOperationObservable: Observable<UserCourse>,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<UserCoursesFeature.Action, UserCoursesFeature.Message>() {
    init {
        compositeDisposable += userCourseOperationObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { onNewMessage(UserCoursesFeature.Message.UserCourseOperationUpdate(it)) },
                onError = emptyOnErrorStub
            )
    }
    override fun handleAction(action: UserCoursesFeature.Action) {
        // no op
    }
}