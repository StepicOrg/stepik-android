package org.stepik.android.view.learning_actions.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_learning_action_wishlist.*
import org.stepic.droid.R
import org.stepik.android.presentation.wishlist.WishlistFeature
import org.stepik.android.view.learning_actions.model.LearningActionsItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate

class WishlistActionAdapterDelegate : AdapterDelegate<LearningActionsItem, DelegateViewHolder<LearningActionsItem>>() {
    override fun isForViewType(position: Int, data: LearningActionsItem): Boolean =
        data is LearningActionsItem.Wishlist

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<LearningActionsItem> {
        val parentWidth = parent.measuredWidth - parent.paddingLeft - parent.paddingRight
        val itemMargin = parent.resources.getDimensionPixelSize(R.dimen.course_item_margin) * 2
        val itemView = createView(parent, R.layout.item_learning_action_wishlist)
        val itemWidth = parentWidth / 2 - itemMargin
        itemView.updateLayoutParams { width = itemWidth }
        return ViewHolder(itemView)
    }

    private class ViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<LearningActionsItem>(containerView), LayoutContainer {

        private val viewStateDelegate = ViewStateDelegate<WishlistFeature.State>()

        init {
            viewStateDelegate.addState<WishlistFeature.State.Idle>()
            viewStateDelegate.addState<WishlistFeature.State.Empty>(wishlistActionTitle, wishlistActionCourseCount)
            viewStateDelegate.addState<WishlistFeature.State.Loading>(wishlistActionTitle, wishlistActionLoadingView)
            viewStateDelegate.addState<WishlistFeature.State.Error>(wishlistActionTitle)
            viewStateDelegate.addState<WishlistFeature.State.Content>(wishlistActionTitle, wishlistActionCourseCount)
        }

        override fun onBind(data: LearningActionsItem) {
            data as LearningActionsItem.Wishlist
            render(data.state)
        }

        private fun render(state: WishlistFeature.State) {
            viewStateDelegate.switchState(state)
            wishlistActionCourseCount.text =
                when (state) {
                    is WishlistFeature.State.Empty ->
                        context.getString(R.string.wishlist_empty)
                    is WishlistFeature.State.Content ->
                        context.resources.getQuantityString(R.plurals.course_count, state.wishListCourses.size, state.wishListCourses.size)
                    else ->
                        ""
                }
        }
    }
}