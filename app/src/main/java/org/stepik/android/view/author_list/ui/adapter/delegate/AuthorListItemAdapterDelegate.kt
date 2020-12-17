package org.stepik.android.view.author_list.ui.adapter.delegate

import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_author.view.*
import kotlinx.android.synthetic.main.layout_author_properties.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepik.android.domain.catalog_block.model.AuthorCatalogBlockContentItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class AuthorListItemAdapterDelegate(
    private val onItemClick: (Long) -> Unit
) : AdapterDelegate<AuthorCatalogBlockContentItem, DelegateViewHolder<AuthorCatalogBlockContentItem>>() {
    override fun isForViewType(position: Int, data: AuthorCatalogBlockContentItem): Boolean =
        data is AuthorCatalogBlockContentItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<AuthorCatalogBlockContentItem> =
        ViewHolder(createView(parent, R.layout.item_author))

    private inner class ViewHolder(root: View) : DelegateViewHolder<AuthorCatalogBlockContentItem>(root) {
        private val authorCoverImageTarget =
            RoundedBitmapImageViewTarget(context.resources.getDimension(R.dimen.corner_radius), root.authorItemImage)

        private val authorPlaceholder = BitmapFactory
            .decodeResource(context.resources, R.drawable.general_placeholder)
            .let { bitmap ->
                RoundedBitmapDrawableFactory
                    .create(context.resources, bitmap)
                    .apply {
                        cornerRadius = context.resources.getDimension(R.dimen.corner_radius)
                    }
            }

        private val authorPropertiesContainer = root.authorPropertiesContainer
        private val authorCourseCount = authorPropertiesContainer.coursesCountText
        private val authorSubscriberCount = authorPropertiesContainer.subscribersCountText
        private val authorTitle = root.authorItemName

        init {
            root.setOnClickListener { itemData?.id?.let(onItemClick) }
        }

        override fun onBind(data: AuthorCatalogBlockContentItem) {
            Glide
                .with(context)
                .asBitmap()
                .load(data.avatar)
                .placeholder(authorPlaceholder)
                .fitCenter()
                .into(authorCoverImageTarget)

            authorTitle.text = data.fullName
            authorCourseCount.text = context.resources.getQuantityString(R.plurals.course_count, data.createdCoursesCount, data.createdCoursesCount)
            authorSubscriberCount.text = "${data.followersCount}" // TODO String res
        }
    }
}