package org.stepic.droid.ui.util

import android.support.v7.widget.SearchView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.web.Api
import java.util.concurrent.TimeUnit

object SearchViewHelper {
    private const val AUTOCOMPLETE_DEBOUNCE_MS = 300L
    
    fun setupSearchViewSuggestionsSources(searchView: AutoCompleteSearchView, api: Api, databaseFacade: DatabaseFacade, onQueryTextSubmit: () -> Unit): CompositeDisposable {
        val compositeDisposable = CompositeDisposable()
        val adapter = searchView.searchQueriesAdapter

        val publisher = PublishSubject.create<String>()

        val queryPublisher = publisher
                .debounce(AUTOCOMPLETE_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())

        compositeDisposable.add(queryPublisher
                .flatMap { query -> Observable.fromCallable { databaseFacade.getSearchQueries(query, 2) }.onErrorResumeNext(Observable.empty()) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ adapter.rawDBItems = it }, { e -> e.printStackTrace() }))

        compositeDisposable.add(queryPublisher
                .flatMap { query -> api.getSearchQueries(query).toObservable().onErrorResumeNext(Observable.empty()) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ adapter.rawAPIItems = it.queries }, { e -> e.printStackTrace() }))


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                onQueryTextSubmit()
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                adapter.constraint = query
                publisher.onNext(query)
                return false
            }
        })

        return compositeDisposable
    }
}