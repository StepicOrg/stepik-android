package org.stepic.droid.ui.custom

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.search_queries_recycler_view.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.adapters.SearchQueriesAdapter


class AutoCompleteSearchView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : SearchView(context, attrs, defStyleAttr) {
    var searchQueriesRecyclerView: RecyclerView? = null
    val searchQueriesAdapter = SearchQueriesAdapter()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        searchQueriesAdapter.searchView = this
    }

    override fun onDetachedFromWindow() {
        searchQueriesAdapter.searchView = null
        super.onDetachedFromWindow()
    }

    fun initSuggestions(rootView: ViewGroup, searchMenuItem: MenuItem) {
        val inflater = LayoutInflater.from(context)
        searchQueriesRecyclerView = inflater.inflate(R.layout.search_queries_recycler_view, rootView, false).searchQueriesRecyclerView
        searchQueriesRecyclerView?.let {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = searchQueriesAdapter

            searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    it.layoutManager?.scrollToPosition(0)
                    it.visibility = View.VISIBLE
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    it.visibility = View.GONE
                    return true
                }

            })

            rootView.addView(it)
        }
    }
}