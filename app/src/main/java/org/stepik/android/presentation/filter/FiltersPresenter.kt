package org.stepik.android.presentation.filter

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.presentation.catalog.model.CatalogItem
import org.stepik.android.view.injection.catalog.FiltersBus
import ru.nobird.android.presentation.base.PresenterBase
import java.util.EnumSet
import javax.inject.Inject

class FiltersPresenter
@Inject
constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,

    private val sharedPreferenceHelper: SharedPreferenceHelper,
    @FiltersBus
    private val filtersPublisher: PublishSubject<EnumSet<StepikFilter>>
) : PresenterBase<FiltersView>(),
    CatalogItem {

    private var state: FiltersView.State =
        FiltersView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    init {
        onNeedFilters()
    }

    override fun attachView(view: FiltersView) {
        super.attachView(view)
        view.setState(state)
    }

    private fun onNeedFilters(forceUpdate: Boolean = false) {
        if (state != FiltersView.State.Idle && !forceUpdate) return
        compositeDisposable += Single.fromCallable { sharedPreferenceHelper.filterForFeatured }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    state =
                        FiltersView.State.FiltersLoaded(
                            it
                        )
                },
                onError = {
                    state =
                        FiltersView.State.Empty
                }
            )
    }

    fun onFilterChanged(newAppliedFilters: EnumSet<StepikFilter>) {
        compositeDisposable += Completable.fromCallable { sharedPreferenceHelper.saveFilterForFeatured(newAppliedFilters) }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = {
                    filtersPublisher.onNext(newAppliedFilters)
                    onNeedFilters(forceUpdate = true)
                },
                onError = emptyOnErrorStub
            )
    }
}