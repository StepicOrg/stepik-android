package org.stepic.droid.ui.adapters

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.search_query_item.view.*
import org.stepic.droid.R
import org.stepic.droid.model.SearchQuery
import org.stepic.droid.model.SearchQuerySource


class SearchQueriesAdapter : RecyclerView.Adapter<SearchQueriesAdapter.SearchQueryViewHolder>() {
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

    private lateinit var querySpan: ForegroundColorSpan

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        querySpan = ForegroundColorSpan(ContextCompat.getColor(recyclerView.context, R.color.search_view_suggestions_prefix_color))
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onBindViewHolder(holder: SearchQueryViewHolder, p: Int) {
        val (query, source) = items[p]

        holder.itemView.searchIcon.setImageResource(
                if (source == SearchQuerySource.DB) {
                    R.drawable.ic_history
                } else {
                    R.drawable.ic_action_search
                }
        )

        holder.itemView.searchQuery.text = query
        holder.itemView.setOnClickListener {
            searchView?.setQuery(query.toString(), true)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            SearchQueryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_query_item, parent, false))


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

    class SearchQueryViewHolder(view: View) : RecyclerView.ViewHolder(view)
}