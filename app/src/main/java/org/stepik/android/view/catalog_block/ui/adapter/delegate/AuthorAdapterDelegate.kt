package org.stepik.android.view.catalog_block.ui.adapter.delegate

import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_author.*
import kotlinx.android.synthetic.main.layout_author_properties.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.util.TextUtil
import org.stepik.android.domain.catalog_block.model.CatalogAuthor
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class AuthorAdapterDelegate(
    private val onItemClick: (Long) -> Unit
) : AdapterDelegate<CatalogAuthor, DelegateViewHolder<CatalogAuthor>>() {
    override fun isForViewType(position: Int, data: CatalogAuthor): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogAuthor> =
        ViewHolder(createView(parent, R.layout.item_author))

    private inner class ViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<CatalogAuthor>(containerView), LayoutContainer {
        private val authorCoverImageTarget =
            RoundedBitmapImageViewTarget(context.resources.getDimension(R.dimen.corner_radius), authorListImage)

        private val authorPlaceholder = BitmapFactory
            .decodeResource(context.resources, R.drawable.general_placeholder)
            .let { bitmap ->
                RoundedBitmapDrawableFactory
                    .create(context.resources, bitmap)
                    .apply {
                        cornerRadius = context.resources.getDimension(R.dimen.corner_radius)
                    }
            }

        private val authorCourseCount = authorListPropertiesContainer.coursesCountText
        private val authorSubscriberCount = authorListPropertiesContainer.subscribersCountText

        init {
            containerView.setOnClickListener { itemData?.id?.let(onItemClick) }
        }

        override fun onBind(data: CatalogAuthor) {
            Glide
                .with(context)
                .asBitmap()
                .load(data.avatar)
                .placeholder(authorPlaceholder)
                .fitCenter()
                .into(authorCoverImageTarget)

            authorListTitle.text = data.fullName
            authorCourseCount.text = context.resources.getQuantityString(R.plurals.course_count, data.createdCoursesCount, data.createdCoursesCount)
            authorSubscriberCount.text = context.resources.getString(R.string.author_subscribers, TextUtil.formatNumbers(data.followersCount.toLong()))
        }
    }
}