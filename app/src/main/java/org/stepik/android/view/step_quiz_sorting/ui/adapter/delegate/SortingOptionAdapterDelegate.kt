package org.stepik.android.view.step_quiz_sorting.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemStepQuizSortingBinding
import org.stepik.android.view.latex.ui.widget.ProgressableWebViewClient
import org.stepik.android.view.step_quiz_sorting.ui.model.SortingOption
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class SortingOptionAdapterDelegate(
    private val adapter: DefaultDelegateAdapter<SortingOption>,
    private val onMoveItemClicked: (position: Int, direction: SortingDirection) -> Unit
) : AdapterDelegate<SortingOption, DelegateViewHolder<SortingOption>>() {
    override fun isForViewType(position: Int, data: SortingOption): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<SortingOption> =
        ViewHolder(createView(parent, R.layout.item_step_quiz_sorting))

    private inner class ViewHolder(root: View) : DelegateViewHolder<SortingOption>(root) {
        private val viewBinding: ItemStepQuizSortingBinding by viewBinding { ItemStepQuizSortingBinding.bind(root) }

        init {
            viewBinding.stepQuizSortingOptionUp.setOnClickListener { onMoveItemClicked(adapterPosition, SortingDirection.UP) }
            viewBinding.stepQuizSortingOptionDown.setOnClickListener { onMoveItemClicked(adapterPosition, SortingDirection.DOWN) }

            viewBinding.stepQuizSortingOption.webViewClient = ProgressableWebViewClient(viewBinding.stepQuizSortingOptionProgress, viewBinding.stepQuizSortingOption.webView)
        }

        override fun onBind(data: SortingOption) {
            itemView.isEnabled = data.isEnabled
            viewBinding.stepQuizSortingOption.setText(data.option)

            viewBinding.stepQuizSortingOptionUp.isEnabled = data.isEnabled && adapterPosition != 0
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
