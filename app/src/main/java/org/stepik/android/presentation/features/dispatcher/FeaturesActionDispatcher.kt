package org.stepik.android.presentation.features.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.feature.interactor.FeaturesInteractor
import org.stepik.android.presentation.features.FeaturesFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class FeaturesActionDispatcher
@Inject
constructor(
    private val featuresInteractor: FeaturesInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<FeaturesFeature.Action, FeaturesFeature.Message>() {
    override fun handleAction(action: FeaturesFeature.Action) {
        when (action) {
            is FeaturesFeature.Action.FetchRubricatorUrl ->
                compositeDisposable += featuresInteractor
                    .fetchRubricatorUrl()
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(FeaturesFeature.Message.FetchRubricatorUrlSuccess(it)) },
                        onError = { onNewMessage(FeaturesFeature.Message.FetchRubricatorUrlError) }
                    )
        }
    }
}