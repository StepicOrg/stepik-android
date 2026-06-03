package org.stepik.android.view.learning_actions.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemLearningActionWishlistBinding
import org.stepik.android.presentation.wishlist.WishlistFeature
import org.stepik.android.view.learning_actions.model.LearningActionsItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate

class WishlistActionAdapterDelegate(private val onClick: () -> Unit) : AdapterDelegate<LearningActionsItem, DelegateViewHolder<LearningActionsItem>>() {
    override fun isForViewType(position: Int, data: LearningActionsItem): Boolean =
        data is LearningActionsItem.Wishlist

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<LearningActionsItem> {
        val parentWidth = parent.measuredWidth - parent.paddingLeft - parent.paddingRight
        val itemMargin = parent.resources.getDimensionPixelSize(R.dimen.course_item_margin) * 2
        val itemView = createView(parent, R.layout.item_learning_action_wishlist)
        val itemWidth = parentWidth / 2 - itemMargin
        itemView.updateLayoutParams { width = itemWidth }
        return ViewHolder(itemView, onClick)
    }

    private class ViewHolder(
        root: View,
        private val onClick: () -> Unit
    ) : DelegateViewHolder<LearningActionsItem>(root) {
        private val viewBinding: ItemLearningActionWishlistBinding by viewBinding { ItemLearningActionWishlistBinding.bind(root) }

        private val viewStateDelegate = ViewStateDelegate<WishlistFeature.State>()

        init {
            viewStateDelegate.addState<WishlistFeature.State.Idle>()
            viewStateDelegate.addState<WishlistFeature.State.Empty>(viewBinding.wishlistActionTitle, viewBinding.wishlistActionCourseCount)
            viewStateDelegate.addState<WishlistFeature.State.Loading>(viewBinding.wishlistActionTitle, viewBinding.wishlistActionLoadingView)
            viewStateDelegate.addState<WishlistFeature.State.Error>(viewBinding.wishlistActionTitle)
            viewStateDelegate.addState<WishlistFeature.State.Content>(viewBinding.wishlistActionTitle, viewBinding.wishlistActionCourseCount)
            itemView.setOnClickListener { onClick() }
        }

        override fun onBind(data: LearningActionsItem) {
            data as LearningActionsItem.Wishlist
            render(data.state)
        }

        private fun render(state: WishlistFeature.State) {
            viewStateDelegate.switchState(state)
            viewBinding.wishlistActionCourseCount.text =
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