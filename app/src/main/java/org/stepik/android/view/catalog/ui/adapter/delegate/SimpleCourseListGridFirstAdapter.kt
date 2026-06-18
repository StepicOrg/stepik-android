package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.ViewGroup
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemBlockSimpleCourseListGridFirstBinding
import org.stepik.android.domain.catalog.model.CatalogCourseList
import org.stepik.android.view.catalog.mapper.CourseCountMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class SimpleCourseListGridFirstAdapter(
    private val courseCountMapper: CourseCountMapper,
    private val onCourseListClicked: (CatalogCourseList) -> Unit
) : AdapterDelegate<CatalogCourseList, DelegateViewHolder<CatalogCourseList>>() {
    override fun isForViewType(position: Int, data: CatalogCourseList): Boolean =
        position == 0

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogCourseList> =
        ViewHolder(createView(parent, R.layout.item_block_simple_course_list_grid_first))

    private inner class ViewHolder(root: android.view.View) : DelegateViewHolder<CatalogCourseList>(root) {
        private val viewBinding: ItemBlockSimpleCourseListGridFirstBinding by viewBinding { ItemBlockSimpleCourseListGridFirstBinding.bind(root) }

        init {
            viewBinding.simpleCourseListGridOverlay.setOnClickListener { onCourseListClicked(itemData ?: return@setOnClickListener) }
        }

        override fun onBind(data: CatalogCourseList) {
            viewBinding.simpleCourseListGridTitle.text = data.title
            viewBinding.simpleCourseListGridCount.text =
                courseCountMapper.mapCourseCountToString(context, data.coursesCount)
        }
    }
}
