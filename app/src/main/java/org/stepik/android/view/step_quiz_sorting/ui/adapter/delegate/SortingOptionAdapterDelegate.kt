package org.stepik.android.view.step_quiz_sorting.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.view.step_quiz_sorting.ui.model.SortingOption
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder

class SortingOptionAdapterDelegate : AdapterDelegate<SortingOption, DelegateViewHolder<SortingOption>>() {
    override fun isForViewType(position: Int, data: SortingOption): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<SortingOption> =
        ViewHolder(createView(parent, R.layout.item_step_quiz_choice))

    class ViewHolder(root: View): DelegateViewHolder<SortingOption>(root)
}