package org.stepik.android.view.onboarding.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_onboarding.*
import org.stepic.droid.R
import org.stepik.android.view.onboarding.model.OnboardingCourseList
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class OnboardingCourseListAdapterDelegate(
    @DrawableRes
    private val iconBackground: Int,
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
            itemIcon.setBackgroundResource(iconBackground)
        }
    }
}