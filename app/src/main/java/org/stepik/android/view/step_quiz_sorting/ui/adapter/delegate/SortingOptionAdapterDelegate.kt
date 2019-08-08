package org.stepik.android.view.step_quiz_sorting.ui.adapter.delegate

import android.support.v4.view.ViewCompat
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_step_quiz_sorting.view.*
import org.stepic.droid.R
import org.stepik.android.view.step_quiz_sorting.ui.model.SortingOption
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder

class SortingOptionAdapterDelegate : AdapterDelegate<SortingOption, DelegateViewHolder<SortingOption>>() {
    override fun isForViewType(position: Int, data: SortingOption): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<SortingOption> =
        ViewHolder(createView(parent, R.layout.item_step_quiz_sorting))

    class ViewHolder(root: View): DelegateViewHolder<SortingOption>(root) {
        private val stepQuizSortingOption = root.stepQuizSortingOption

        init {
            ViewCompat.setElevation(root, context.resources.getDimension(R.dimen.step_quiz_sorting_item_elevation))
        }

        override fun onBind(data: SortingOption) {
            stepQuizSortingOption.text = data.option
        }
    }
}