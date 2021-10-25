package org.stepik.android.view.debug.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_split_group.*
import org.stepic.droid.R
import org.stepic.droid.analytic.experiments.SplitTest
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.debug.model.SplitGroupData
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class SplitGroupAdapterDelegate(
    private val onItemClick: (String, String, List<String>) -> Unit
) : AdapterDelegate<SplitGroupData, DelegateViewHolder<SplitGroupData>>() {
    override fun isForViewType(position: Int, data: SplitGroupData): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<SplitGroupData> =
        ViewHolder(createView(parent, R.layout.item_split_group))

    private inner class ViewHolder(override val containerView: View) : DelegateViewHolder<SplitGroupData>(containerView), LayoutContainer {
        init {
            containerView.setOnClickListener { itemData?.let { onItemClick(it.splitTestName, it.splitTestValue, it.splitTestGroups) } }
        }
        override fun onBind(data: SplitGroupData) {
            splitTestGroupTitle.text = data.splitTestName
            splitTestGroupValue.text = data.splitTestValue
        }
    }
}