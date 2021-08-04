package org.stepik.android.presentation.debug.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.debug.interactor.DebugInteractor
import org.stepik.android.domain.debug.model.DebugSettings
import org.stepik.android.presentation.debug.DebugFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class DebugActionDispatcher
@Inject
constructor(
    private val debugInteractor: DebugInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<DebugFeature.Action, DebugFeature.Message>() {
    override fun handleAction(action: DebugFeature.Action) {
        when (action) {
            is DebugFeature.Action.FetchDebugSettings -> {
                compositeDisposable += debugInteractor
                    .getFirebaseToken()
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = {
                            val debugSettings = DebugSettings(fcmToken = it)
                            onNewMessage(DebugFeature.Message.FetchDebugSettingsSuccess(debugSettings))
                        },
                        onError = { onNewMessage(DebugFeature.Message.FetchDebugSettingsFailure) }
                    )
            }
        }
    }
}