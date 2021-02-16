package org.stepik.android.view.course_list.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_collection_similar_courses_list.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.domain.catalog.model.CatalogCourseList
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.catalog.mapper.CourseCountMapper
import org.stepik.android.view.catalog.ui.adapter.delegate.SimpleCourseListDefaultAdapterDelegate
import ru.nobird.android.core.model.cast
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
        ViewHolder(createView(parent, R.layout.item_collection_similar_courses_list))

    private inner class ViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<CourseListItem>(containerView), LayoutContainer {
        private val adapter = DefaultDelegateAdapter<CatalogCourseList>()
            .also {
                it += SimpleCourseListDefaultAdapterDelegate(courseCountMapper, onCourseListClicked)
            }

        init {
            val rowCount = 1
            courseListsRecycler.layoutManager =
                TableLayoutManager(
                    context,
                    horizontalSpanCount = context.resources.getInteger(R.integer.simple_course_lists_default_columns),
                    verticalSpanCount = rowCount,
                    orientation = LinearLayoutManager.HORIZONTAL,
                    reverseLayout = false
                )
            courseListsRecycler.setRecycledViewPool(sharedViewPool)
            courseListsRecycler.setHasFixedSize(true)
            courseListsRecycler.adapter = adapter

            val snapHelper = CoursesSnapHelper(rowCount)
            snapHelper.attachToRecyclerView(courseListsRecycler)
        }

        override fun onBind(data: CourseListItem) {
            val courseLists = data
                .cast<CourseListItem.SimilarCourses>()
                .similarCourses

            adapter.items = courseLists
        }
    }
}