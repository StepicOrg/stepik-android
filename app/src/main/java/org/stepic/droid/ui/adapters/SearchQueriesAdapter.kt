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


class SearchQueriesAdapter : RecyclerView.Adapter<SearchQueriesAdapter.SearchQueryViewHolder>() {
    private var items: List<SearchQuery> = emptyList()
    private var rawItems: List<SearchQuery> = emptyList()
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

        holder.itemView.searchQuery.text = spannable
        holder.itemView.setOnClickListener {
            searchView?.setQuery(query.text, true)
        }
    }

    fun replace(collection: List<SearchQuery>) {
        rawItems = collection
        filterItems()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            SearchQueryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_query_item, parent, false))


    override fun getItemCount() = items.size

    private fun filterItems() {
        items = rawItems.filter { it.text.contains(constraint, ignoreCase = true) }
        notifyDataSetChanged()
    }

    class SearchQueryViewHolder(view: View) : RecyclerView.ViewHolder(view)
}