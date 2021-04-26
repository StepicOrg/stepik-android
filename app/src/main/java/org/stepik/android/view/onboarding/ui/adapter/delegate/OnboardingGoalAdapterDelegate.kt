package org.stepik.android.view.onboarding.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_onboarding.*
import org.stepic.droid.R
import org.stepik.android.view.onboarding.model.IconBackground
import org.stepik.android.view.onboarding.model.OnboardingGoal
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class OnboardingGoalAdapterDelegate(
    private val onItemClicked: (OnboardingGoal) -> Unit
) : AdapterDelegate<OnboardingGoal, DelegateViewHolder<OnboardingGoal>>() {
    override fun isForViewType(position: Int, data: OnboardingGoal): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<OnboardingGoal> =
        ViewHolder(createView(parent, R.layout.item_onboarding))

    private inner class ViewHolder(override val containerView: View) : DelegateViewHolder<OnboardingGoal>(containerView), LayoutContainer {
        init {
            itemView.setOnClickListener { itemData?.let(onItemClicked) }
        }

        override fun onBind(data: OnboardingGoal) {
            itemIcon.text = data.icon
            itemTitle.text = data.title
            val backgroundRes = IconBackground.values()[adapterPosition % IconBackground.values().size].backgroundRes
            itemIcon.setBackgroundResource(backgroundRes)
        }
    }
}