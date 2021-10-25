package org.stepik.android.presentation.debug.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.debug.interactor.SplitGroupInteractor
import org.stepik.android.presentation.debug.SplitGroupFeature
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class SplitGroupActionDispatcher
@Inject
constructor(
    private val splitGroupInteractor: SplitGroupInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<SplitGroupFeature.Action, SplitGroupFeature.Message>() {
    override fun handleAction(action: SplitGroupFeature.Action) {
        when (action) {
            is SplitGroupFeature.Action.FetchSplitGroupData -> {
                compositeDisposable += splitGroupInteractor
                    .getSplitGroupsList(action.splitGroups)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(SplitGroupFeature.Message.InitSuccess(it)) },
                        onError = emptyOnErrorStub
                    )
            }

            is SplitGroupFeature.Action.SetSplitGroupData -> {
                compositeDisposable += splitGroupInteractor
                    .updateSplitGroupData(action.splitGroupData)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onComplete = { onNewMessage(SplitGroupFeature.Message.SetSplitGroupDataSuccess(action.splitGroupData)) },
                        onError = emptyOnErrorStub
                    )
            }
        }
    }
}