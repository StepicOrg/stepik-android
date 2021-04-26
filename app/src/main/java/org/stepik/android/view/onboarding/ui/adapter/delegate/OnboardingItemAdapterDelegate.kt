package org.stepik.android.view.onboarding.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_onboarding.*
import org.stepic.droid.R
import org.stepik.android.view.onboarding.model.OnboardingGoalItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class OnboardingItemAdapterDelegate(
    private val onItemClicked: (OnboardingGoalItem) -> Unit
) : AdapterDelegate<OnboardingGoalItem, DelegateViewHolder<OnboardingGoalItem>>() {
    override fun isForViewType(position: Int, data: OnboardingGoalItem): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<OnboardingGoalItem> =
        ViewHolder(createView(parent, R.layout.item_onboarding))

    private inner class ViewHolder(override val containerView: View) : DelegateViewHolder<OnboardingGoalItem>(containerView), LayoutContainer {
        init {
            itemView.setOnClickListener { itemData?.let(onItemClicked) }
        }

        override fun onBind(data: OnboardingGoalItem) {
            val (icon, title) = data.itemTitle.split(' ', limit = 2)
            itemIcon.text = icon
            itemTitle.text = title
            itemIcon.setBackgroundResource(data.backgroundResId)
        }
    }
}