package org.stepic.droid.core.presenters

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.contracts.SearchSuggestionsView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.model.SearchQuerySource
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.web.Api
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class SearchSuggestionsPresenter
@Inject constructor(
        private val api: Api,
        private val databaseFacade: DatabaseFacade,
        private val analytic: Analytic,
        @BackgroundScheduler
        private val scheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
        ) : PresenterBase<SearchSuggestionsView>() {

    companion object {
        private const val AUTOCOMPLETE_DEBOUNCE_MS = 300L
        private const val DB_ELEMENTS_COUNT = 2
    }

    private val compositeDisposable = CompositeDisposable()
    private val publisher = PublishSubject.create<String>()

    private var needSkipAmplitudeEvent = false

    override fun attachView(view: SearchSuggestionsView) {
        super.attachView(view)
        initSearchView(view)
    }

    private fun initSearchView(searchView: SearchSuggestionsView) {
        val queryPublisher = publisher
                .debounce(AUTOCOMPLETE_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
                .subscribeOn(scheduler)

        compositeDisposable.add(queryPublisher
                .flatMap { query -> Observable.fromCallable { databaseFacade.getSearchQueries(query, DB_ELEMENTS_COUNT) } }
                .observeOn(mainScheduler)
                .subscribe { searchView.setSuggestions(it, SearchQuerySource.DB) })

        compositeDisposable.add(queryPublisher
                .flatMap { query -> api.getSearchQueries(query).toObservable().onErrorResumeNext(Observable.empty()) }
                .observeOn(mainScheduler)
                .subscribe { searchView.setSuggestions(it.queries, SearchQuerySource.API) })
    }

    fun onQueryTextChange(query: String) {
        publisher.onNext(query)
    }

    fun onNeedSkipAmplitudeEvent() {
        needSkipAmplitudeEvent = true
    }

    fun onQueryTextSubmit(query: String) {
        analytic.reportEventWithName(Analytic.Search.SEARCH_SUBMITTED, query)
        if (needSkipAmplitudeEvent) {
            needSkipAmplitudeEvent = false
        } else {
            analytic.reportAmplitudeEvent(AmplitudeAnalytic.Search.SEARCHED, mapOf(AmplitudeAnalytic.Search.Params.QUERY to query.toLowerCase()))
        }
    }

    override fun detachView(view: SearchSuggestionsView) {
        compositeDisposable.clear()
        super.detachView(view)
    }
}