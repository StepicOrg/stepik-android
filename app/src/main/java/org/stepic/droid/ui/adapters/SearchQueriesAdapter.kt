package org.stepic.droid.ui.adapters

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
    private var items: List<SearchQuery> = emptyList()

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

    override fun onBindViewHolder(holder: SearchQueryViewHolder, p: Int) {
        val query = items[p]
        val spannable = SpannableString(query.text)

        val spanStart = query.text.indexOf(constraint, ignoreCase = true)
        if (spanStart != -1) {
            spannable.setSpan(ForegroundColorSpan(0x44000000), spanStart, spanStart + constraint.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        holder.itemView.searchIcon.setImageResource(
                if (query.source == SearchQuerySource.DB) {
                    R.drawable.ic_history
                } else {
                    R.drawable.ic_action_search
                }
        )

        holder.itemView.searchQuery.text = spannable
        holder.itemView.setOnClickListener {
            searchView?.setQuery(query.text, true)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            SearchQueryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_query_item, parent, false))


    override fun getItemCount() = items.size

    private fun filterItems() {
        items = (rawDBItems + rawAPIItems)
                .filter { it.text.contains(constraint, ignoreCase = true) }
                .distinctBy { it.text.toLowerCase() }
        notifyDataSetChanged()
    }

    class SearchQueryViewHolder(view: View) : RecyclerView.ViewHolder(view)
}