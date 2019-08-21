package org.stepik.android.view.step_quiz_matching.ui.adapter.delegate

import android.support.v4.view.ViewCompat
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_step_quiz_sorting.view.*
import org.stepic.droid.R
import org.stepik.android.view.step_quiz_matching.ui.model.MatchingItem
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter

class MatchingItemOptionAdapterDelegate(
    private val adapter: DefaultDelegateAdapter<MatchingItem>,
    private val onMoveItemClicked: (position: Int, direction: SortingDirection) -> Unit
) : AdapterDelegate<MatchingItem, DelegateViewHolder<MatchingItem>>() {
    override fun isForViewType(position: Int, data: MatchingItem): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<MatchingItem> =
        ViewHolder(createView(parent, R.layout.item_step_quiz_sorting))

    private inner class ViewHolder(root: View) : DelegateViewHolder<MatchingItem>(root) {
        private val stepQuizSortingOption = root.stepQuizSortingOption
        private val stepQuizSortingOptionUp = root.stepQuizSortingOptionUp
        private val stepQuizSortingOptionDown = root.stepQuizSortingOptionDown

        init {
            stepQuizSortingOption.setTextSize(16f)

            stepQuizSortingOptionUp.setOnClickListener { onMoveItemClicked(adapterPosition, SortingDirection.UP) }
            stepQuizSortingOptionDown.setOnClickListener { onMoveItemClicked(adapterPosition, SortingDirection.DOWN) }

            root.layoutParams =
                (root.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    leftMargin = context.resources.getDimensionPixelOffset(R.dimen.step_quiz_matching_item_margin)
                }
        }

        override fun onBind(data: MatchingItem) {
            data as MatchingItem.Option

            stepQuizSortingOption.setPlainOrLaTeXText(data.text)

            stepQuizSortingOptionUp.isEnabled = data.isEnabled && adapterPosition != 1
            stepQuizSortingOptionUp.alpha = if (stepQuizSortingOptionUp.isEnabled) 1f else 0.2f

            stepQuizSortingOptionDown.isEnabled = data.isEnabled && adapterPosition + 1 != adapter.items.size
            stepQuizSortingOptionDown.alpha = if (stepQuizSortingOptionDown.isEnabled) 1f else 0.2f

            val elevation = if (data.isEnabled) context.resources.getDimension(R.dimen.step_quiz_sorting_item_elevation) else 0f
            ViewCompat.setElevation(itemView, elevation)
        }
    }

    enum class SortingDirection {
        UP, DOWN
    }
}