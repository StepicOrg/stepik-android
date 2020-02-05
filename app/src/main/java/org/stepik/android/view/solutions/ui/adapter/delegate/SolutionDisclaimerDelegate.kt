package org.stepik.android.view.solutions.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_solution_information.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepik.android.domain.solutions.model.SolutionItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class SolutionDisclaimerDelegate : AdapterDelegate<SolutionItem, DelegateViewHolder<SolutionItem>>() {
    override fun isForViewType(position: Int, data: SolutionItem): Boolean =
        data is SolutionItem.Disclaimer

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<SolutionItem> =
        ViewHolder(createView(parent, R.layout.item_solution_information))

    private inner class ViewHolder(root: View) : DelegateViewHolder<SolutionItem>(root) {

        private val solutionsFeedback = root.solutionsFeedback

        override fun onBind(data: SolutionItem) {
            data as SolutionItem.Disclaimer
            solutionsFeedback.setCompoundDrawables(start = R.drawable.ic_step_quiz_validation)
        }
    }
}