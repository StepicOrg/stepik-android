package org.stepik.android.view.debug.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemSplitTestDataBinding
import org.stepik.android.domain.debug.model.SplitTestData
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class SplitTestDataAdapterDelegate(
    private val onItemClick: (String, String, List<String>) -> Unit
) : AdapterDelegate<SplitTestData, DelegateViewHolder<SplitTestData>>() {
    override fun isForViewType(position: Int, data: SplitTestData): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<SplitTestData> =
        ViewHolder(createView(parent, R.layout.item_split_test_data))

    private inner class ViewHolder(containerView: View) : DelegateViewHolder<SplitTestData>(containerView) {
        private val viewBinding: ItemSplitTestDataBinding by viewBinding { ItemSplitTestDataBinding.bind(itemView) }

        init {
            containerView.setOnClickListener { itemData?.let { onItemClick(it.splitTestName, it.splitTestValue, it.splitTestGroups) } }
        }
        override fun onBind(data: SplitTestData) {
            viewBinding.splitTestGroupTitle.text = data.splitTestName
            viewBinding.splitTestGroupValue.text = data.splitTestValue
        }
    }
}
