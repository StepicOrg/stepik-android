package org.stepic.droid.ui.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import android.widget.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.search_queries_recycler_view.view.*
import org.stepic.droid.R
import org.stepic.droid.base.CoursesDatabaseFragmentBase
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.adapters.SearchQueriesAdapter
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.liftM2

open class FindCoursesFragment: CoursesDatabaseFragmentBase() {
    companion object {
        fun newInstance() = FindCoursesFragment()
    }

    private var mAdapter: SearchQueriesAdapter? = null
    private lateinit var searchQueriesRecyclerView: RecyclerView

    private var searchView: SearchView? = null
    private var searchMenuItem: MenuItem? = null
    private var handledByRoot = false

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

        mAdapter = SearchQueriesAdapter()
        searchQueriesRecyclerView = layoutInflater.inflate(R.layout.search_queries_recycler_view, rootView, false).searchQueriesRecyclerView
        searchQueriesRecyclerView.layoutManager = LinearLayoutManager(context)
        searchQueriesRecyclerView.adapter = mAdapter
        searchQueriesRecyclerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                collapseAndHide(false)
            } else {
                hideSoftKeypad() // such compromise to avoid using adjustResize on parent activity
            }
            false
        }


        rootView.addView(searchQueriesRecyclerView)
    }

    override fun onDestroyView() {
        listOfCoursesView.onFocusChangeListener = null
        mAdapter = null
        super.onDestroyView()
    }

    private fun collapseAndHide(rootHandle: Boolean) {
        searchView.liftM2(searchMenuItem) { _, menuItem ->
            if (menuItem.isActionViewExpanded) {
                if (rootHandle) handledByRoot = true
                hideSoftKeypad()
                menuItem.collapseActionView()
                searchQueriesRecyclerView.visibility = View.GONE
            }
        }
    }

    override fun getCourseType() = Table.featured

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

        val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu.findItem(R.id.action_search)

        searchView = searchMenuItem?.actionView as? SearchView
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
                    populateAdapter(newText)
                    return false
                }
            })

            it.setOnSearchClickListener {
                searchQueriesRecyclerView.layoutManager?.scrollToPosition(0)
                searchQueriesRecyclerView.visibility = View.VISIBLE
            }

            mAdapter?.searchView = it
        }

    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        searchView?.setOnQueryTextListener(null)
        searchView = null
    }


    @StringRes
    protected open fun getTitle() = R.string.catalog_title

    private fun populateAdapter(query: String) {
        mAdapter?.constraint = query
        api.getSearchQueries(query).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe({
            Log.d(javaClass.canonicalName, it.toString())
            mAdapter?.replace(it.queries)
        }, { it.printStackTrace() })
    }
}