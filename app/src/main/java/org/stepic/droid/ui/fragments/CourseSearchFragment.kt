package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.SearchCoursesPresenter
import org.stepic.droid.model.CourseListType
import org.stepic.droid.ui.custom.AutoCompleteSearchView
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
    private var searchView: AutoCompleteSearchView? = null

    @Inject
    lateinit var searchCoursesPresenter: SearchCoursesPresenter

    override fun injectComponent() {
        App
            .componentManager()
            .courseGeneralComponent()
            .courseListComponentBuilder()
            .build()
            .inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        searchQuery = arguments?.getString(QUERY_KEY)
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
        searchView?.setOnQueryTextListener(null)
        searchView = null
        super.onDestroyView()
    }

    override fun getCourseType(): CourseListType? = null

    public override fun showEmptyScreen(isShown: Boolean) {
        if (isShown) {
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
        searchView.setSearchable(requireActivity())
        searchView.initSuggestions(rootView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.onSubmitted(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                searchView.setConstraint(query)
                return false
            }
        })

        searchMenuItem.expandActionView()
        searchQuery?.let { searchView.setQuery(it, false) }
        searchView.clearFocus()

        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean = true

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                activity?.finish()
                return true
            }
        })
        this.searchView = searchView
    }

    override fun onNeedDownloadNextPage() {
        searchCoursesPresenter.downloadData(searchQuery)
    }

    override fun onRefresh() {
        searchCoursesPresenter.refreshData(searchQuery)
    }
}