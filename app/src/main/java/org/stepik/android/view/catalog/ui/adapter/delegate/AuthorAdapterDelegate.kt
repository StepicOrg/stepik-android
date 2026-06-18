package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.ViewGroup
import dev.androidbroadcast.vbpd.viewBinding
import com.bumptech.glide.Glide
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemAuthorBinding
import org.stepic.droid.util.TextUtil
import org.stepik.android.domain.catalog.model.CatalogAuthor
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class AuthorAdapterDelegate(
    private val onItemClick: (Long) -> Unit
) : AdapterDelegate<CatalogAuthor, DelegateViewHolder<CatalogAuthor>>() {
    override fun isForViewType(position: Int, data: CatalogAuthor): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogAuthor> =
        ViewHolder(createView(parent, R.layout.item_author))

    private inner class ViewHolder(root: android.view.View) : DelegateViewHolder<CatalogAuthor>(root) {
        private val viewBinding: ItemAuthorBinding by viewBinding { ItemAuthorBinding.bind(root) }

        private val authorCourseCount = viewBinding.authorListPropertiesContainer.coursesCountText
        private val authorSubscriberCount = viewBinding.authorListPropertiesContainer.subscribersCountText

        init {
            root.setOnClickListener { itemData?.id?.let(onItemClick) }
        }

        override fun onBind(data: CatalogAuthor) {
            Glide
                .with(context)
                .asBitmap()
                .load(data.avatar)
                .placeholder(R.drawable.general_placeholder)
                .fitCenter()
                .into(viewBinding.authorListImage)

            viewBinding.authorListTitle.text = data.fullName
            authorCourseCount.text = context.resources.getQuantityString(R.plurals.course_count, data.createdCoursesCount, data.createdCoursesCount)
            authorSubscriberCount.text = context.resources.getString(R.string.author_subscribers, TextUtil.formatNumbers(data.followersCount.toLong()))
        }
    }
}