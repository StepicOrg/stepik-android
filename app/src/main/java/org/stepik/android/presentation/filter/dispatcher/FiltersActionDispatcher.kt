package org.stepik.android.presentation.filter.dispatcher

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.filter.analytic.ContenLanguageChangedAnalyticEvent
import org.stepik.android.presentation.filter.FiltersFeature
import org.stepik.android.view.injection.catalog.FiltersBus
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import java.util.EnumSet
import javax.inject.Inject

class FiltersActionDispatcher
@Inject
constructor(
    private val analytic: Analytic,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    @FiltersBus
    private val filtersObservable: Observable<EnumSet<StepikFilter>>,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<FiltersFeature.Action, FiltersFeature.Message>() {
    init {
        compositeDisposable += filtersObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { onNewMessage(FiltersFeature.Message.InitMessage(forceUpdate = true)) },
                onError = emptyOnErrorStub
            )
    }
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

            is FiltersFeature.Action.ChangeFilters -> {
                val newLanguage = action.filters.firstOrNull()?.language
                if (newLanguage != null) {
                    analytic.report(ContenLanguageChangedAnalyticEvent(newLanguage, ContenLanguageChangedAnalyticEvent.Source.CATALOG))
                }

                compositeDisposable += Completable.fromCallable { sharedPreferenceHelper.saveFilterForFeatured(action.filters) }
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onComplete = { onNewMessage(FiltersFeature.Message.LoadFiltersSuccess(action.filters)) },
                        onError = emptyOnErrorStub
                    )
            }
        }
    }
}