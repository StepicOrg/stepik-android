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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import org.stepic.droid.R
import org.stepic.droid.ui.adapters.SearchQueriesAdapter


class AutoCompleteSearchView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : SearchView(context, attrs, defStyleAttr) {
    val searchQueriesAdapter = SearchQueriesAdapter(context)
    var suggestionsOnTouchListener: OnTouchListener? = null

    private val closeIcon: ImageView = findViewById(R.id.search_close_btn)

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
        val searchQueriesRecyclerView = inflater.inflate(R.layout.search_queries_recycler_view, rootView, false) as RecyclerView
        searchQueriesRecyclerView.layoutManager = LinearLayoutManager(context)
        searchQueriesRecyclerView.adapter = searchQueriesAdapter

        searchQueriesRecyclerView.setOnTouchListener { v, event ->
            if (searchQueriesRecyclerView.findChildViewUnder(event.x, event.y) == null) {
                if (event.action == MotionEvent.ACTION_UP
                        && event.x > 0 && event.y > 0
                        && event.x < v.width && event.y < v.height) { // to track events only inside view
                    this@AutoCompleteSearchView.clearFocus()
                }
            } else {
                suggestionsOnTouchListener?.onTouch(v, event)
            }
            false
        }

        setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchQueriesRecyclerView.layoutManager?.scrollToPosition(0)
                searchQueriesRecyclerView.visibility = View.VISIBLE
            } else {
                searchQueriesRecyclerView.visibility = View.GONE
            }
        }

        rootView.addView(searchQueriesRecyclerView)

    }

    fun setCloseIconDrawableRes(@DrawableRes iconRes: Int) {
        closeIcon.setImageResource(iconRes)
    }

    fun setSearchable(activity: Activity) {
        val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val componentName = activity.componentName
        val searchableInfo = searchManager.getSearchableInfo(componentName)
        setSearchableInfo(searchableInfo)
    }
}