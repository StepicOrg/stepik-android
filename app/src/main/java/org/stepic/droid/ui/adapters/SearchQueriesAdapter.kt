package org.stepic.droid.ui.adapters

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.search_query_item.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.model.SearchQuery
import org.stepic.droid.model.SearchQuerySource
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.listeners.OnItemClickListener
import org.stepic.droid.util.resolveColorAttribute
import ru.nobird.android.view.base.ui.extension.inflate
import javax.inject.Inject

class SearchQueriesAdapter(context: Context) : RecyclerView.Adapter<SearchQueriesAdapter.SearchQueryViewHolder>(), OnItemClickListener {
    @Inject
    lateinit var analytic: Analytic

    init {
        App.component().inject(this)
    }

    private var items: List<Pair<Spannable, SearchQuerySource>> = emptyList()

    var rawDBItems: List<SearchQuery> = emptyList()
        set(value) {
            field = value
            filterItems()
        }

    var rawAPIItems: List<SearchQuery> = emptyList()
        set(value) {
            field = value
            filterItems()
        }

    var constraint: String = ""
        set(value) {
            field = value
            filterItems()
        }

    var searchView: SearchView? = null
    var suggestionClickCallback: AutoCompleteSearchView.SuggestionClickCallback? = null

    private val querySpan = BackgroundColorSpan(context.resolveColorAttribute(R.attr.colorControlHighlight))

    override fun onBindViewHolder(holder: SearchQueryViewHolder, p: Int) {
        val (query, source) = items[p]

        holder.searchIcon.setImageResource(source.iconRes)
        holder.searchQuery.text = query
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchQueryViewHolder =
        SearchQueryViewHolder(parent.inflate(R.layout.search_query_item, false), this)

    override fun onItemClick(position: Int) {
        if (position < 0 || position >= items.size) {
            return
        }
        val (query, _) = items[position]
        analytic.reportEventValue(Analytic.Search.SEARCH_SUGGESTION_CLICKED, (query.length - constraint.length).toLong())
        suggestionClickCallback?.onQueryTextSubmitSuggestion(query.toString())
    }


    override fun getItemCount() = items.size

    private fun filterItems() {
        items = (rawDBItems + rawAPIItems)
                .filter { it.text.contains(constraint, ignoreCase = true) }
                .distinctBy { it.text.toLowerCase() }
                .map {
                    val spannable = SpannableString(it.text)
                    val spanStart = it.text.indexOf(constraint, ignoreCase = true)
                    if (spanStart != -1) {
                        spannable.setSpan(querySpan, spanStart, spanStart + constraint.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    spannable to it.source
                }
        notifyDataSetChanged()
    }

    class SearchQueryViewHolder(view: View, onItemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(view) {
        val searchQuery: TextView = view.searchQuery
        val searchIcon: ImageView = view.searchIcon

        init {
            itemView.setOnClickListener {
                onItemClickListener.onItemClick(adapterPosition)
            }
        }
    }
}