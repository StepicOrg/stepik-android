package org.stepic.droid.ui.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import org.stepic.droid.R
import org.stepic.droid.base.CoursesDatabaseFragmentBase
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.liftM2

open class FindCoursesFragment: CoursesDatabaseFragmentBase() {
    companion object {
        fun newInstance() = FindCoursesFragment()
    }

    private var searchView: SearchView? = null
    private var menuItem: MenuItem? = null
    private var handledByRoot = false

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(getTitle(), false)
        rootView.setParentTouchEvent { collapseAndHide(true) }

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
        searchView.liftM2(menuItem) { _, menuItem ->
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

        val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        menuItem = menu.findItem(R.id.action_search)

        searchView = menuItem?.actionView as SearchView
        searchView?.let {
            val closeImageView: ImageView = it.findViewById(R.id.search_close_btn)
            closeImageView.setImageDrawable(ContextCompat.getDrawable(context, getCloseIconDrawableRes()))

            val componentName = activity.componentName
            val searchableInfo = searchManager.getSearchableInfo(componentName)
            it.setSearchableInfo(searchableInfo)
            it.maxWidth = 20000
            it.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    collapseAndHide(false)
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        }

    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        searchView?.setOnQueryTextListener(null)
        searchView = null
    }


    @StringRes
    protected open fun getTitle() = R.string.catalog_title
}