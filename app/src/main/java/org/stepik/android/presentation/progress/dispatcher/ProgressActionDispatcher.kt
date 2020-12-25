package org.stepik.android.presentation.progress.dispatcher

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.model.Progress
import org.stepik.android.presentation.progress.ProgressFeature
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class ProgressActionDispatcher
@Inject
constructor(
    progressObservable: Observable<Progress>,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<ProgressFeature.Action, ProgressFeature.Message>() {
    init {
        compositeDisposable += progressObservable
            .subscribeOn(backgroundScheduler)
            .subscribeOn(mainScheduler)
            .subscribeBy(
                onNext = { onNewMessage(ProgressFeature.Message.ProgressUpdate(it)) },
                onError = emptyOnErrorStub
            )
    }
    override fun handleAction(action: ProgressFeature.Action) {
        // no op
    }
}