package org.stepik.android.presentation.debug.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.debug.interactor.SplitTestsInteractor
import org.stepik.android.presentation.debug.SplitTestsFeature
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class SplitTestsActionDispatcher
@Inject
constructor(
    private val splitTestsInteractor: SplitTestsInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<SplitTestsFeature.Action, SplitTestsFeature.Message>() {
    override fun handleAction(action: SplitTestsFeature.Action) {
        when (action) {
            is SplitTestsFeature.Action.FetchSplitTestData -> {
                compositeDisposable += splitTestsInteractor
                    .getSplitTestDataList()
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(SplitTestsFeature.Message.InitSuccess(it)) },
                        onError = emptyOnErrorStub
                    )
            }

            is SplitTestsFeature.Action.SetSplitTestData -> {
                compositeDisposable += splitTestsInteractor
                    .updateSplitTestData(action.splitTestData)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onComplete = { onNewMessage(SplitTestsFeature.Message.SetSplitTestDataSuccess(action.splitTestData)) },
                        onError = emptyOnErrorStub
                    )
            }
        }
    }
}