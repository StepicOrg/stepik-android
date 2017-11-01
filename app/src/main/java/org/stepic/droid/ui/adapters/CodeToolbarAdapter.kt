package org.stepic.droid.ui.adapters

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.stepic.droid.R
import org.stepic.droid.code.highlight.themes.Presets
import org.stepic.droid.model.code.symbolsForLanguage
import org.stepic.droid.ui.listeners.OnItemClickListener

class CodeToolbarAdapter(private val context: Context) : RecyclerView.Adapter<CodeToolbarAdapter.CodeToolbarItem>() {

    interface OnSymbolClickListener {
        fun onSymbolClick(symbol: String)
    }

    var autoCompleteWords: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var autoCompletePrefix: String? = null

    var onSymbolClickListener: OnSymbolClickListener? = null
    private var symbols: Array<String> = emptyArray()
    private val onItemClickListener = OnItemClickListener { position ->
        val autocompleteSize = autoCompleteWords.size
        val symbol = if (position < autocompleteSize) {
            val word = autoCompleteWords[position]
            val pref = autoCompletePrefix
            if (pref != null) {
                word.removePrefix(pref) + " "
            } else {
                ""
            }
        } else {
            symbols[position - autocompleteSize]
        }
        onSymbolClickListener?.onSymbolClick(symbol)
    }

    fun setLanguage(language: String) {
        symbols = symbolsForLanguage(language, context)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodeToolbarItem? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_code_toolbar_item, parent, false)
        return CodeToolbarItem(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: CodeToolbarItem, position: Int) {
        val autocompleteSize = autoCompleteWords.size
        if (position < autocompleteSize) {
            val word = SpannableString(autoCompleteWords[position])
            autoCompletePrefix?.let {
                word.setSpan(BackgroundColorSpan(Presets.themes[0].bracketsHighlight), 0, it.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            holder.bindData(word)
        } else {
            holder.bindData(SpannableString(symbols[position - autocompleteSize]))
        }
    }

    override fun getItemCount(): Int = symbols.size + autoCompleteWords.size

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

}
