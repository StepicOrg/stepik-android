package org.stepik.android.view.step_quiz_code.ui.adapter.delegate

import android.view.ViewGroup
import android.widget.TextView
import org.stepic.droid.R
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder

class CodeLangAdapterDelegate(
    private val onCodeLangClicked: (String) -> Unit
) : AdapterDelegate<String, DelegateViewHolder<String>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<String> =
        ViewHolder(createView(parent, R.layout.item_step_quiz_code_lang) as TextView)

    override fun isForViewType(position: Int, data: String): Boolean =
       true

    private inner class ViewHolder(private val root: TextView) : DelegateViewHolder<String>(root) {
        init {
            root.setOnClickListener { itemData?.let(onCodeLangClicked) }
        }

        override fun onBind(data: String) {
            root.text = data
        }
    }
}