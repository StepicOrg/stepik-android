package org.stepik.android.presentation.filter.dispatcher

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.presentation.filter.FiltersFeature
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class FiltersActionDispatcher
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<FiltersFeature.Action, FiltersFeature.Message>() {
    override fun handleAction(action: FiltersFeature.Action) {
        when (action) {
            is FiltersFeature.Action.LoadFilters ->
                compositeDisposable += Single.fromCallable { sharedPreferenceHelper.filterForFeatured }
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(FiltersFeature.Message.LoadFiltersSuccess(it)) },
                        onError = { onNewMessage(FiltersFeature.Message.LoadFiltersError) }
                    )

            is FiltersFeature.Action.ChangeFilters ->
                compositeDisposable += Completable.fromCallable { sharedPreferenceHelper.saveFilterForFeatured(action.filters) }
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onComplete = { onNewMessage(FiltersFeature.Message.FiltersChanged(action.filters)) },
                        onError = emptyOnErrorStub
                    )
        }
    }
}