package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_courses.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.experiments.CatalogSearchSplitTest
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.SearchCoursesPresenter
import org.stepic.droid.model.CourseListType
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.util.initCenteredToolbar
import timber.log.Timber
import javax.inject.Inject

class CourseSearchFragment: CourseListFragmentBase(), AutoCompleteSearchView.FocusCallback {
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

    @Inject
    lateinit var catalogSearchSplitTest: CatalogSearchSplitTest

    lateinit var searchIcon: ImageView

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
        searchIcon = searchViewToolbar.findViewById(androidx.appcompat.R.id.search_mag_icon) as ImageView
        swipeRefreshLayout.post { searchCoursesPresenter.downloadData(searchQuery) }
        if (true) {
//        if (catalogSearchSplitTest.currentGroup.isUpdatedSearchVisible) {
            setupCatalogABSearchBar()
        }
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
        setupSearchView(searchView, searchMenuItem)
        this.searchView = searchView
    }

    override fun onNeedDownloadNextPage() {
        searchCoursesPresenter.downloadData(searchQuery)
    }

    override fun onRefresh() {
        searchCoursesPresenter.refreshData(searchQuery)
    }

    private fun setupCatalogABSearchBar() {
        centeredToolbar.isVisible = false
        if (android.os.Build.VERSION.SDK_INT < 21) {
            toolbarShadow.isVisible = true
        }
        searchViewToolbar.isVisible = true
        setupSearchView(searchViewToolbar)
        searchViewToolbar.setIconifiedByDefault(false)
        searchViewToolbar.setFocusCallback(this)
        backIcon.setOnClickListener {
            searchViewToolbar.onActionViewCollapsed()
            searchViewToolbar.onActionViewExpanded()
            searchViewToolbar.clearFocus()
        }
    }

    override fun onFocusChanged(hasFocus: Boolean) {
        Timber.d("Focus: $hasFocus")
        backIcon.isVisible = hasFocus
        if (hasFocus) {
            searchIcon.setImageResource(0)
            (searchViewToolbar.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 0)
            searchViewToolbar.setBackgroundResource(R.color.white)
        } else {
            searchIcon.setImageResource(R.drawable.ic_action_search)
            (searchViewToolbar.layoutParams as ViewGroup.MarginLayoutParams).setMargins(4, 4, 4, 4)
            searchViewToolbar.setBackgroundResource(R.drawable.bg_catalog_search_bar)
        }
    }

    private fun setupSearchView(searchView: AutoCompleteSearchView, searchMenuItem: MenuItem? = null) {
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

        if (searchMenuItem == null) {
            searchView.onActionViewExpanded()
        } else {
            searchMenuItem.expandActionView()
            searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean = true

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    activity?.finish()
                    return true
                }
            })
        }
        searchQuery?.let { searchView.setQuery(it, false) }
        searchView.clearFocus()
    }
}