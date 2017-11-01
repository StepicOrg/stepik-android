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

    var autoCompleteWords: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var autoCompletePrefix: String? = null

    private val autocompleteItemsCount
        get() = autoCompleteWords.size + if (autoCompleteWords.isNotEmpty()) 1 else 0

    var onSymbolClickListener: OnSymbolClickListener? = null
    private var symbols: Array<String> = emptyArray()
    private val onItemClickListener = OnItemClickListener { position ->
        val symbol = if (position < autocompleteItemsCount - 1) {
            autoCompletePrefix?.let {
                autoCompleteWords[position].removePrefix(it) + " "
            } ?: ""
        } else {
            symbols[position - autocompleteItemsCount]
        }
        onSymbolClickListener?.onSymbolClick(symbol)
    }

    fun setLanguage(language: String) {
        symbols = symbolsForLanguage(language, context)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) =
            if (autoCompleteWords.isNotEmpty() && position == autoCompleteWords.size) {
                SEPARATOR_VIEW_TYPE
            } else {
                ELEMENT_VIEW_TYPE
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
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
            ELEMENT_VIEW_TYPE -> (holder as CodeToolbarItem).let {
                if (autoCompleteWords.isNotEmpty() && position < autocompleteItemsCount) {
                    val word = SpannableString(autoCompleteWords[position])
                    autoCompletePrefix?.let {
                        word.setSpan(autocompletePrefixBackgroundSpan, 0, it.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    it.bindData(word)
                } else {
                    it.bindData(SpannableString(symbols[position - autocompleteItemsCount]))
                }
            }
        }
    }

    override fun getItemCount(): Int = symbols.size + autocompleteItemsCount

    class CodeToolbarItem(itemView: View, onItemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        private val codeToolbarSymbol = itemView.findViewById<TextView>(R.id.codeToolbarSymbol)

        init {
            itemView.setOnClickListener {
                onItemClickListener.onItemClick(adapterPosition)
            }
            codeToolbarSymbol.typeface = Typeface.MONOSPACE
        }

        fun bindData(symbol: SpannableString) {
            codeToolbarSymbol.text = symbol
        }
    }
    class CodeToolbarSeparator(view: View) : RecyclerView.ViewHolder(view)

}
