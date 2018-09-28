package org.stepic.droid.ui.adapters

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.stepic.droid.R
import org.stepic.droid.code.data.AutocompleteState
import org.stepic.droid.model.code.symbolsForLanguage
import org.stepic.droid.ui.listeners.OnItemClickListener

class CodeToolbarAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val SEPARATOR_VIEW_TYPE = 1
        private const val ELEMENT_VIEW_TYPE = 0
    }

    interface OnSymbolClickListener {
        fun onSymbolClick(symbol: String)
    }

    private val autocompletePrefixBackgroundSpan by lazy {
        BackgroundColorSpan(ContextCompat.getColor(context, R.color.code_toolbar_autocomplete_prefix_background))
    }

    private var recyclerView: RecyclerView? = null
    private var items: MutableList<Spannable?> = ArrayList()
    private var symbols: Array<String> = emptyArray()
    var autocomplete = AutocompleteState("", emptyList())
        set(value) {
            val old = field.words.size
            val new = value.words.size
            field = value
            notifyDataChanged(0, old, new)
            if (value.words.isNotEmpty()) {
                recyclerView?.layoutManager?.scrollToPosition(0)
            }
        }

    var onSymbolClickListener: OnSymbolClickListener? = null
    private val onItemClickListener: OnItemClickListener = object : OnItemClickListener {
        override fun onItemClick(position: Int) {
            items[position]?.toString()?.let { word ->
                onSymbolClickListener?.onSymbolClick(
                        if (autocomplete.prefix.isNotEmpty() && word.startsWith(autocomplete.prefix)) {
                            word.removePrefix(autocomplete.prefix) + " "
                        } else {
                            word
                        })
            }
        }
    }

    fun setLanguage(language: String) {
        val old = symbols.size
        symbols = symbolsForLanguage(language, context)
        val new = symbols.size
        notifyDataChanged(items.size - symbols.size, old, new)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun getItemViewType(position: Int) =
            if (items[position] == null) {
                SEPARATOR_VIEW_TYPE
            } else {
                ELEMENT_VIEW_TYPE
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            SEPARATOR_VIEW_TYPE ->
                CodeToolbarSeparator(inflater.inflate(R.layout.view_code_toolbar_separator, parent, false))

            ELEMENT_VIEW_TYPE ->
                CodeToolbarItem(inflater.inflate(R.layout.view_code_toolbar_item, parent, false), onItemClickListener)

            else -> throw IllegalArgumentException("Wrong view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ELEMENT_VIEW_TYPE ->
                if (holder is CodeToolbarItem) {
                    items[position]?.let { holder.bindData(it) }
                }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun invalidateItems() {
        items.clear()
        items.addAll(autocomplete.words.map {
            val word = SpannableString(it)
            word.setSpan(autocompletePrefixBackgroundSpan, 0, autocomplete.prefix.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            word
        })
        if (autocomplete.words.isNotEmpty() && symbols.isNotEmpty()) {
            items.add(null) // add separator
        }
        items.addAll(symbols.map { SpannableString(it) })
    }

    private fun notifyDataChanged(start: Int, oldSize: Int, newSize: Int) {
        invalidateItems()
        val changed = minOf(oldSize, newSize)
        var delta = Math.abs(oldSize - newSize)
        var offset = start

        if (changed == 0 && delta != 0) { // separator should be added or removed
            delta++
            if (offset > 0) offset--
        }

        if (oldSize > newSize) {
            notifyItemRangeRemoved(offset + changed, delta)
        } else if (oldSize < newSize) {
            notifyItemRangeInserted(offset + changed, delta)
        }

        notifyItemRangeChanged(offset, changed)
    }

    private class CodeToolbarItem(itemView: View, onItemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        private val codeToolbarSymbol = itemView.findViewById<TextView>(R.id.codeToolbarSymbol)

        init {
            itemView.setOnClickListener {
                onItemClickListener.onItemClick(adapterPosition)
            }
            codeToolbarSymbol.typeface = Typeface.MONOSPACE
        }

        fun bindData(symbol: Spannable) {
            codeToolbarSymbol.text = symbol
        }
    }

    private class CodeToolbarSeparator(view: View) : RecyclerView.ViewHolder(view)

}
