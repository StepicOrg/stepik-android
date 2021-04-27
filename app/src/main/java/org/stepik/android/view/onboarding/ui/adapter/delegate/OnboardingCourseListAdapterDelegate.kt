package org.stepik.android.view.onboarding.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_onboarding.*
import org.stepic.droid.R
import org.stepik.android.view.onboarding.model.OnboardingCourseList
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.view.base.ui.extension.toPx

class OnboardingCourseListAdapterDelegate(
    private val onItemClicked: (OnboardingCourseList) -> Unit
) : AdapterDelegate<OnboardingCourseList, DelegateViewHolder<OnboardingCourseList>>() {
    override fun isForViewType(position: Int, data: OnboardingCourseList): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<OnboardingCourseList> =
        ViewHolder(createView(parent, R.layout.item_onboarding))

    private inner class ViewHolder(override val containerView: View) : DelegateViewHolder<OnboardingCourseList>(containerView), LayoutContainer {
        init {
            itemView.setOnClickListener { itemData?.let(onItemClicked) }
        }

        override fun onBind(data: OnboardingCourseList) {
            itemIcon.text = data.icon
            itemTitle.text = data.title
            itemIcon.setBackgroundResource(R.drawable.onboarding_goal_yellow_green_gradient)
            // TODO APPS-3292: Add gradient stroke
            if (data.isFeatured) {
                val cardView = (itemView as MaterialCardView)
                cardView.strokeWidth = 2.toPx()
                cardView.strokeColor = ContextCompat.getColor(context, R.color.color_overlay_red)
            }
        }
    }
}