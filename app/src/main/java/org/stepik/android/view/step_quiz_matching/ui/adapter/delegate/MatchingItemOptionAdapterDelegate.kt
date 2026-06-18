package org.stepik.android.view.step_quiz_matching.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemStepQuizSortingBinding
import org.stepik.android.view.latex.ui.widget.ProgressableWebViewClient
import org.stepik.android.view.step_quiz_matching.ui.model.MatchingItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class MatchingItemOptionAdapterDelegate(
    private val adapter: DefaultDelegateAdapter<MatchingItem>,
    private val onMoveItemClicked: (position: Int, direction: SortingDirection) -> Unit
) : AdapterDelegate<MatchingItem, DelegateViewHolder<MatchingItem>>() {
    override fun isForViewType(position: Int, data: MatchingItem): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<MatchingItem> =
        ViewHolder(createView(parent, R.layout.item_step_quiz_sorting))

    private inner class ViewHolder(root: View) : DelegateViewHolder<MatchingItem>(root) {
        private val viewBinding: ItemStepQuizSortingBinding by viewBinding { ItemStepQuizSortingBinding.bind(root) }

        init {
            viewBinding.stepQuizSortingOptionUp.setOnClickListener { onMoveItemClicked(adapterPosition, SortingDirection.UP) }
            viewBinding.stepQuizSortingOptionDown.setOnClickListener { onMoveItemClicked(adapterPosition, SortingDirection.DOWN) }

            root.layoutParams =
                (root.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    leftMargin = context.resources.getDimensionPixelOffset(R.dimen.step_quiz_matching_item_margin)
                }

            viewBinding.stepQuizSortingOption.webViewClient = ProgressableWebViewClient(viewBinding.stepQuizSortingOptionProgress, viewBinding.stepQuizSortingOption.webView)
        }

        override fun onBind(data: MatchingItem) {
            data as MatchingItem.Option
            itemView.isEnabled = data.isEnabled

            viewBinding.stepQuizSortingOption.setText(data.text)

            viewBinding.stepQuizSortingOptionUp.isEnabled = data.isEnabled && adapterPosition != 1
            viewBinding.stepQuizSortingOptionUp.alpha = if (viewBinding.stepQuizSortingOptionUp.isEnabled) 1f else 0.2f

            viewBinding.stepQuizSortingOptionDown.isEnabled = data.isEnabled && adapterPosition + 1 != adapter.items.size
            viewBinding.stepQuizSortingOptionDown.alpha = if (viewBinding.stepQuizSortingOptionDown.isEnabled) 1f else 0.2f

            val elevation = if (data.isEnabled) context.resources.getDimension(R.dimen.step_quiz_sorting_item_elevation) else 0f
            ViewCompat.setElevation(itemView, elevation)
        }
    }

    enum class SortingDirection {
        UP, DOWN
    }
}
