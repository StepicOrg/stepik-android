package org.stepik.android.view.catalog_block.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_simple_course_list_default.*
import org.stepic.droid.R
import org.stepik.android.domain.catalog_block.model.StandardCatalogBlockContentItem
import org.stepik.android.view.catalog_block.mapper.CourseCountMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class SimpleCourseListDefaultAdapterDelegate(
    private val courseCountMapper: CourseCountMapper,
    private val onCourseListClicked: (StandardCatalogBlockContentItem) -> Unit
) : AdapterDelegate<StandardCatalogBlockContentItem, DelegateViewHolder<StandardCatalogBlockContentItem>>() {
    override fun isForViewType(position: Int, data: StandardCatalogBlockContentItem): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<StandardCatalogBlockContentItem> =
        ViewHolder(createView(parent, R.layout.item_simple_course_list_default))

    private inner class ViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<StandardCatalogBlockContentItem>(containerView), LayoutContainer {
        private val colorSchemes =
            listOf(
                R.color.color_overlay_green,
                R.color.color_overlay_yellow,
                R.color.color_overlay_blue,
                R.color.color_overlay_violet
            ).map { AppCompatResources.getColorStateList(context, it) }

        init {
            containerView.setOnClickListener { onCourseListClicked(itemData ?: return@setOnClickListener) }
        }

        override fun onBind(data: StandardCatalogBlockContentItem) {
            simpleCourseListTitle.text = data.title
            simpleCourseListCount.text =
                courseCountMapper.mapCourseCountToString(context, data.coursesCount)

            val colorList = colorSchemes[adapterPosition % colorSchemes.size]

            simpleCourseListTitle.setTextColor(colorList)
            simpleCourseListCount.setTextColor(colorList)

            ViewCompat.setBackgroundTintList(itemView, colorList)
        }
    }
}