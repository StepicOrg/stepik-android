package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.widget.SearchView
import android.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.CoursesDatabaseFragmentBase
import org.stepic.droid.core.presenters.SearchSuggestionsPresenter
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.util.initCenteredToolbar
import javax.inject.Inject

open class FindCoursesFragment: CoursesDatabaseFragmentBase() {
    companion object {
        fun newInstance() = FindCoursesFragment()
    }

    @Inject
    lateinit var searchSuggestionsPresenter: SearchSuggestionsPresenter

    private var searchView: AutoCompleteSearchView? = null
    private var searchMenuItem: MenuItem? = null

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

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(getTitle(), false)
        listOfCoursesView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchMenuItem?.collapseActionView()
            }
        }
    }

    override fun onDestroyView() {
        listOfCoursesView.onFocusChangeListener = null
        searchView?.let {
            searchSuggestionsPresenter.detachView(it)
            it.setOnQueryTextListener(null)
            searchView = null
        }
        searchMenuItem = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        searchSuggestionsPresenter.refreshSuggestions()
    }

    override fun getCourseType() = Table.featured

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

        searchMenuItem = menu.findItem(R.id.action_search)
        searchView = searchMenuItem?.actionView as? AutoCompleteSearchView

        searchMenuItem?.setOnMenuItemClickListener {
            analytic.reportEvent(Analytic.Search.SEARCH_OPENED)
            false
        }

        searchView?.let {
            it.initSuggestions(rootView)
            it.setCloseIconDrawableRes(getCloseIconDrawableRes())
            it.setSearchable(activity)

            it.suggestionsOnTouchListener = View.OnTouchListener { _, _ ->
                hideSoftKeypad()
                false
            }

            searchSuggestionsPresenter.attachView(it)

            it.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    searchSuggestionsPresenter.onQueryTextSubmit(query)
                    searchMenuItem?.collapseActionView()
                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {
                    searchSuggestionsPresenter.onQueryTextChange(query)
                    return false
                }
            })
        }
    }

    @StringRes
    protected open fun getTitle() = R.string.catalog_title

}