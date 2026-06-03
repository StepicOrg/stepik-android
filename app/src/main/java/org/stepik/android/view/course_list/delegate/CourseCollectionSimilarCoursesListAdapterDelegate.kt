package org.stepik.android.view.course_list.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemCollectionHorizontalListBinding
import org.stepik.android.domain.catalog.model.CatalogCourseList
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.catalog.mapper.CourseCountMapper
import org.stepik.android.view.catalog.ui.adapter.delegate.SimpleCourseListDefaultAdapterDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseCollectionSimilarCoursesListAdapterDelegate(
    private val courseCountMapper: CourseCountMapper,
    private val onCourseListClicked: (CatalogCourseList) -> Unit
) : AdapterDelegate<CourseListItem, DelegateViewHolder<CourseListItem>>() {
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun isForViewType(position: Int, data: CourseListItem): Boolean =
        data is CourseListItem.SimilarCourses

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseListItem> =
        ViewHolder(createView(parent, R.layout.item_collection_horizontal_list))

    private inner class ViewHolder(root: View) : DelegateViewHolder<CourseListItem>(root) {
        private val viewBinding: ItemCollectionHorizontalListBinding by viewBinding { ItemCollectionHorizontalListBinding.bind(root) }

        private val adapter = DefaultDelegateAdapter<CatalogCourseList>()
            .also {
                it += SimpleCourseListDefaultAdapterDelegate(courseCountMapper, onCourseListClicked)
            }

        init {
            val rowCount = 1
            viewBinding.horizontalListRecycler.layoutManager =
                TableLayoutManager(
                    context,
                    horizontalSpanCount = context.resources.getInteger(R.integer.simple_course_lists_default_columns),
                    verticalSpanCount = rowCount,
                    orientation = LinearLayoutManager.HORIZONTAL,
                    reverseLayout = false
                )
            viewBinding.horizontalListRecycler.setRecycledViewPool(sharedViewPool)
            viewBinding.horizontalListRecycler.setHasFixedSize(true)
            viewBinding.horizontalListRecycler.adapter = adapter

            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(viewBinding.horizontalListRecycler)
        }

        override fun onBind(data: CourseListItem) {
            data as CourseListItem.SimilarCourses

            viewBinding.containerTitle.setText(R.string.similar_courses_title)

            adapter.items = data.similarCourses
        }
    }
}