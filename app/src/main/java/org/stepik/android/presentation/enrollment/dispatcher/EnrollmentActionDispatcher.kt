package org.stepik.android.presentation.enrollment.dispatcher

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.model.Course
import org.stepik.android.presentation.enrollment.EnrollmentFeature
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class EnrollmentActionDispatcher
@Inject
constructor(
    @EnrollmentCourseUpdates
    enrollmentUpdatesObservable: Observable<Course>,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<EnrollmentFeature.Action, EnrollmentFeature.Message>() {
    init {
        compositeDisposable += enrollmentUpdatesObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { onNewMessage(EnrollmentFeature.Message.EnrollmentMessage(it)) },
                onError = emptyOnErrorStub
            )
    }
    override fun handleAction(action: EnrollmentFeature.Action) {
        // no op
    }
}