package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.widget.SearchView
import android.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.CoursesDatabaseFragmentBase
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.util.initCenteredToolbar

open class FindCoursesFragment: CoursesDatabaseFragmentBase() {
    companion object {
        fun newInstance() = FindCoursesFragment()
    }

    private var searchView: AutoCompleteSearchView? = null
    private var searchMenuItem: MenuItem? = null

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
        searchView?.setOnQueryTextListener(null)
        searchView = null
        searchMenuItem = null
        super.onDestroyView()
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

            it.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    it.onSubmitted(query)
                    searchMenuItem?.collapseActionView()
                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {
                    it.setConstraint(query)
                    return false
                }
            })
        }
    }

    @StringRes
    protected open fun getTitle() = R.string.catalog_title

}