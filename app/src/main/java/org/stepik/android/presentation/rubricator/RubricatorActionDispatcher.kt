package org.stepik.android.presentation.rubricator

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.rubricator.interactor.RubricatorInteractor
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class RubricatorActionDispatcher
@Inject
constructor(
    private val rubricatorInteractor: RubricatorInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<RubricatorFeature.Action, RubricatorFeature.Message>() {
    override fun handleAction(action: RubricatorFeature.Action) {
        when (action) {
            is RubricatorFeature.Action.FetchRubricatorData -> {
                compositeDisposable += rubricatorInteractor
                    .getRubricatorData(action.rubricatorUrl)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(RubricatorFeature.Message.FetchRubricatorDataSuccess(it)) },
                        onError = { onNewMessage(RubricatorFeature.Message.FetchRubricatorDataError) }
                    )
            }

            else -> Unit
        }
    }
}