package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.annotation.StringRes
import android.view.*
import io.reactivex.disposables.CompositeDisposable
import org.stepic.droid.R
import org.stepic.droid.base.CoursesDatabaseFragmentBase
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.util.SearchViewHelper
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.liftM2

open class FindCoursesFragment: CoursesDatabaseFragmentBase() {
    companion object {
        fun newInstance() = FindCoursesFragment()
    }


    private var searchView: AutoCompleteSearchView? = null
    private var searchMenuItem: MenuItem? = null
    private var handledByRoot = false
    private var compositeDisposable: CompositeDisposable? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(getTitle(), false)

        listOfCoursesView.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (!handledByRoot) {
                    collapseAndHide(false)
                }
                handledByRoot = false
            }
        }
    }

    override fun onDestroyView() {
        listOfCoursesView.onFocusChangeListener = null
        super.onDestroyView()
    }

    private fun collapseAndHide(rootHandle: Boolean) {
        searchView.liftM2(searchMenuItem) { _, menuItem ->
            if (menuItem.isActionViewExpanded) {
                if (rootHandle) handledByRoot = true
                hideSoftKeypad()
                menuItem.collapseActionView()
            }
        }
    }

    override fun getCourseType() = Table.featured

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

        searchMenuItem = menu.findItem(R.id.action_search)
        searchView = searchMenuItem?.actionView as? AutoCompleteSearchView

        searchView?.let { it ->
            it.initSuggestions(rootView)
            it.setCloseIconDrawableRes(getCloseIconDrawableRes())
            it.setSearchable(activity)

            it.searchQueriesRecyclerView?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    it.clearFocus()
                } else {
                    hideSoftKeypad() // such compromise to avoid using adjustResize on parent activity
                }
                false
            }

            compositeDisposable = SearchViewHelper.setupSearchViewSuggestionsSources(
                    it, api, databaseFacade, onQueryTextSubmit = {
                collapseAndHide(false)
            })
        }
    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        searchView?.setOnQueryTextListener(null)
        searchView = null
        compositeDisposable?.dispose()
        compositeDisposable = null
    }


    @StringRes
    protected open fun getTitle() = R.string.catalog_title

}