package org.stepic.droid.ui.custom

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.support.annotation.DrawableRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

    init {
        maxWidth = 20000
    }

    fun initSuggestions(rootView: ViewGroup) {
        val inflater = LayoutInflater.from(context)
        searchQueriesRecyclerView = inflater.inflate(R.layout.search_queries_recycler_view, rootView, false).searchQueriesRecyclerView
        searchQueriesRecyclerView?.let {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = searchQueriesAdapter

            this.setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    it.layoutManager?.scrollToPosition(0)
                    it.visibility = View.VISIBLE
                } else {
                    it.visibility = View.GONE
                }
            }

            rootView.addView(it)
        }
    }

    fun setCloseIconDrawableRes(@DrawableRes iconRes: Int) {
        findViewById<ImageView>(R.id.search_close_btn)?.setImageResource(iconRes)
    }

    fun setSearchable(activity: Activity) {
        val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val componentName = activity.componentName
        val searchableInfo = searchManager.getSearchableInfo(componentName)
        setSearchableInfo(searchableInfo)
    }
}