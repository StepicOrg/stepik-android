package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemSimpleCourseListDefaultBinding
import org.stepik.android.domain.catalog.model.CatalogCourseList
import org.stepik.android.view.catalog.mapper.CourseCountMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class SimpleCourseListDefaultAdapterDelegate(
    private val courseCountMapper: CourseCountMapper,
    private val onCourseListClicked: (CatalogCourseList) -> Unit
) : AdapterDelegate<CatalogCourseList, DelegateViewHolder<CatalogCourseList>>() {
    override fun isForViewType(position: Int, data: CatalogCourseList): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogCourseList> =
        ViewHolder(createView(parent, R.layout.item_simple_course_list_default))

    private inner class ViewHolder(root: android.view.View) : DelegateViewHolder<CatalogCourseList>(root) {
        private val viewBinding: ItemSimpleCourseListDefaultBinding by viewBinding { ItemSimpleCourseListDefaultBinding.bind(root) }

        private val colorSchemes =
            listOf(
                R.color.color_overlay_green,
                R.color.color_overlay_yellow,
                R.color.color_overlay_blue,
                R.color.color_overlay_violet
            ).map { AppCompatResources.getColorStateList(context, it) }

        init {
            root.setOnClickListener { onCourseListClicked(itemData ?: return@setOnClickListener) }
        }

        override fun onBind(data: CatalogCourseList) {
            viewBinding.simpleCourseListTitle.text = data.title
            viewBinding.simpleCourseListCount.text =
                courseCountMapper.mapCourseCountToString(context, data.coursesCount)

            val colorList = colorSchemes[adapterPosition % colorSchemes.size]

            viewBinding.simpleCourseListTitle.setTextColor(colorList)
            viewBinding.simpleCourseListCount.setTextColor(colorList)

            ViewCompat.setBackgroundTintList(itemView, colorList)
        }
    }
}
