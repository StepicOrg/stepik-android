package org.stepik.android.presentation.debug.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.debug.interactor.InAppPurchasesInteractor
import org.stepik.android.presentation.debug.InAppPurchasesFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class InAppPurchasesActionDispatcher
@Inject
constructor(
    private val inAppPurchasesInteractor: InAppPurchasesInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<InAppPurchasesFeature.Action, InAppPurchasesFeature.Message>() {
    override fun handleAction(action: InAppPurchasesFeature.Action) {
        compositeDisposable += inAppPurchasesInteractor
            .getAllPurchases()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { onNewMessage(InAppPurchasesFeature.Message.FetchPurchasesSuccess(it)) },
                onError = { onNewMessage(InAppPurchasesFeature.Message.FetchPurchasesFailure) }
            )
    }
}