package org.stepic.droid.ui.custom

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.SearchSuggestionsPresenter
import org.stepic.droid.core.presenters.contracts.SearchSuggestionsView
import org.stepic.droid.model.SearchQuery
import org.stepic.droid.model.SearchQuerySource
import org.stepic.droid.ui.adapters.SearchQueriesAdapter
import javax.inject.Inject

class AutoCompleteSearchView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : SearchView(context, attrs, defStyleAttr), SearchSuggestionsView {
    private val searchQueriesAdapter = SearchQueriesAdapter(context)
    private val closeIcon: ImageView = findViewById(R.id.search_close_btn)

    var suggestionsOnTouchListener: OnTouchListener? = null

    @Inject
    lateinit var searchSuggestionsPresenter: SearchSuggestionsPresenter

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        searchQueriesAdapter.searchView = this
        searchSuggestionsPresenter.attachView(this)
        refreshSuggestions()
    }

    override fun onDetachedFromWindow() {
        searchSuggestionsPresenter.detachView(this)
        searchQueriesAdapter.searchView = null
        super.onDetachedFromWindow()
    }

    init {
        maxWidth = 20000
        App.component().inject(this)
    }

    fun initSuggestions(rootView: ViewGroup) {
        val inflater = LayoutInflater.from(context)
        val searchQueriesRecyclerView = inflater.inflate(R.layout.search_queries_recycler_view, rootView, false) as RecyclerView
        searchQueriesRecyclerView.layoutManager = LinearLayoutManager(context)
        searchQueriesRecyclerView.adapter = searchQueriesAdapter

        searchQueriesRecyclerView.setOnTouchListener { v, event ->
            if (searchQueriesRecyclerView.findChildViewUnder(event.x, event.y) == null) {
                if (event.action == MotionEvent.ACTION_UP && isEventInsideView(v, event)) {
                    this@AutoCompleteSearchView.clearFocus()
                }
            } else {
                suggestionsOnTouchListener?.onTouch(v, event)
            }
            false
        }

        setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                setConstraint(query.toString())
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

    fun setConstraint(constraint: String) {
        searchQueriesAdapter.constraint = constraint
        refreshSuggestions()
    }

    fun onSubmitted(constraint: String) {
        searchSuggestionsPresenter.onQueryTextSubmit(constraint)
    }

    private fun refreshSuggestions() {
        searchSuggestionsPresenter.onQueryTextChange(searchQueriesAdapter.constraint)
    }

    override fun setSuggestions(suggestions: List<SearchQuery>, source: SearchQuerySource) {
        when (source) {
            SearchQuerySource.API ->
                    searchQueriesAdapter.rawAPIItems = suggestions
            SearchQuerySource.DB ->
                    searchQueriesAdapter.rawDBItems = suggestions
        }
    }

    private fun isEventInsideView(v: View, event: MotionEvent) =
            event.x > 0 && event.y > 0
            && event.x < v.width && event.y < v.height
}