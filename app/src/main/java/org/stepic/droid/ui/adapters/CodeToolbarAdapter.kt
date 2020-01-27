package org.stepic.droid.ui.adapters

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.stepic.droid.R
import org.stepic.droid.code.data.AutocompleteState
import org.stepic.droid.model.code.symbolsForLanguage
import kotlin.math.abs

class CodeToolbarAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val SEPARATOR_VIEW_TYPE = 1
        private const val ELEMENT_VIEW_TYPE = 0
    }

    interface OnSymbolClickListener {
        fun onSymbolClick(symbol: String, offset: Int = 0)
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
    private val onItemClickListener: (CharSequence) -> Unit = { symbol ->
        val word = symbol.toString()
        if (autocomplete.prefix.isNotEmpty() && word.startsWith(autocomplete.prefix, ignoreCase = true)) {
            onSymbolClickListener?.onSymbolClick("$word ", autocomplete.prefix.length)
        } else {
            onSymbolClickListener?.onSymbolClick(word)
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
                    items[position]?.let { holder.bind(it) }
                }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is CodeToolbarItem) {
            holder.unbind()
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
        var delta = abs(oldSize - newSize)
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

    private class CodeToolbarItem(itemView: View, onItemClickListener: (symbol: CharSequence) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val codeToolbarSymbol = itemView.findViewById<TextView>(R.id.codeToolbarSymbol)

        private var itemData: Spannable? = null

        init {
            itemView.setOnClickListener {
                itemData?.let(onItemClickListener)
            }
            codeToolbarSymbol.typeface = Typeface.MONOSPACE
        }

        fun bind(symbol: Spannable) {
            itemData = symbol
            codeToolbarSymbol.text = symbol
        }

        fun unbind() {
            itemData = null
        }
    }

    private class CodeToolbarSeparator(view: View) : RecyclerView.ViewHolder(view)

}
