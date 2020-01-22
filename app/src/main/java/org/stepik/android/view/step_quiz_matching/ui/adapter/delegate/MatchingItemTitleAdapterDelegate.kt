package org.stepik.android.view.step_quiz_matching.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.item_step_quiz_sorting.view.*
import org.stepic.droid.R
import org.stepik.android.view.latex.ui.widget.ProgressableWebViewClient
import org.stepik.android.view.step_quiz_matching.ui.model.MatchingItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class MatchingItemTitleAdapterDelegate : AdapterDelegate<MatchingItem, DelegateViewHolder<MatchingItem>>() {
    override fun isForViewType(position: Int, data: MatchingItem): Boolean =
        data is MatchingItem.Title

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<MatchingItem> =
        ViewHolder(createView(parent, R.layout.item_step_quiz_sorting))

    private inner class ViewHolder(root: View) : DelegateViewHolder<MatchingItem>(root) {
        private val stepQuizSortingOption = root.stepQuizSortingOption
        private val stepQuizSortingOptionProgress = root.stepQuizSortingOptionProgress

        private val stepQuizSortingOptionUp = root.stepQuizSortingOptionUp
        private val stepQuizSortingOptionDown = root.stepQuizSortingOptionDown

        init {

            stepQuizSortingOptionUp.isVisible = false
            stepQuizSortingOptionDown.isVisible = false

            root.layoutParams =
                (root.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    rightMargin = context.resources.getDimensionPixelOffset(R.dimen.step_quiz_matching_item_margin)
                }

            stepQuizSortingOption.webViewClient = ProgressableWebViewClient(stepQuizSortingOptionProgress, stepQuizSortingOption.webView)
        }

        override fun onBind(data: MatchingItem) {
            data as MatchingItem.Title
            itemView.isEnabled = data.isEnabled

            stepQuizSortingOption.setText(data.text)

            val elevation = if (data.isEnabled) context.resources.getDimension(R.dimen.step_quiz_sorting_item_elevation) else 0f
            ViewCompat.setElevation(itemView, elevation)
        }
    }
}