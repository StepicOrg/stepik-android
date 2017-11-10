package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import io.reactivex.disposables.CompositeDisposable
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.SearchCoursesPresenter
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.util.SearchViewHelper
import org.stepic.droid.ui.util.initCenteredToolbar
import javax.inject.Inject

class CourseSearchFragment: CourseListFragmentBase() {
    companion object {
        private const val QUERY_KEY = "query_key"

        fun newInstance(query: String): Fragment {
            val fragment = CourseSearchFragment()
            val bundle = Bundle()
            bundle.putString(QUERY_KEY, query)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var searchQuery: String? = null

    @Inject
    lateinit var searchCoursesPresenter: SearchCoursesPresenter

    private var searchSuggestionsDisposable: CompositeDisposable? = null

    override fun injectComponent() {
        App
                .componentManager()
                .courseGeneralComponent()
                .courseListComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onReleaseComponent() {
        App
                .componentManager()
                .releaseCourseGeneralComponent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        searchQuery = arguments.getString(QUERY_KEY)
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(R.string.search_title, true)
        emptySearch.isClickable = false
        emptySearch.isFocusable = false
        searchCoursesPresenter.attachView(this)
        searchCoursesPresenter.restoreState()
        swipeRefreshLayout.post { searchCoursesPresenter.downloadData(searchQuery) }
    }

    override fun onDestroyView() {
        searchCoursesPresenter.detachView(this)
        super.onDestroyView()
    }

    override fun getCourseType(): Table? = null

    public override fun showEmptyScreen(isShowed: Boolean) {
        if (isShowed) {
            emptySearch.visibility = View.VISIBLE
            swipeRefreshLayout.visibility = View.GONE
        } else {
            emptySearch.visibility = View.GONE
            swipeRefreshLayout.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val searchMenuItem = menu.findItem(R.id.action_search)
        val searchView = searchMenuItem.actionView as AutoCompleteSearchView

        searchView.setCloseIconDrawableRes(getCloseIconDrawableRes())
        searchView.setSearchable(activity)
        searchView.initSuggestions(rootView)

        searchSuggestionsDisposable = SearchViewHelper.setupSearchViewSuggestionsSources(searchView, api, databaseFacade, analytic, null)

        searchMenuItem.expandActionView()
        searchQuery?.let { searchView.setQuery(it, false) }
        searchView.clearFocus()

        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean = true

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                activity.finish()
                return true
            }
        })
    }

    override fun onDestroyOptionsMenu() {
        searchSuggestionsDisposable?.dispose()
        super.onDestroyOptionsMenu()
    }

    override fun onNeedDownloadNextPage() {
        searchCoursesPresenter.downloadData(searchQuery)
    }

    override fun onRefresh() {
        searchCoursesPresenter.refreshData(searchQuery)
    }
}